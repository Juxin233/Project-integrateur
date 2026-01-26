package fr.insa.projectIntegrateur.DatabaseService.utils;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class PostgreUpdate {

    private static final String JDBC_URL = "jdbc:postgresql://srv-bdens:5432/bd3a_ng_60_base";
    private static final String DB_LOGIN = "bd3a_ng_60_log";
    private static final String DB_PASS  = "etuu8Kee";

    private static final int BATCH_HEARTBEAT = 1000;

    private PostgreUpdate() {}

    /**
     * @param jsonStream Input JSON：
     *                   1) ：[ { ... }, { ... } ]
     *                   include：
     *                   osm_way_id, from_node, to_node,
     *                   (mode==0) risque_pieton,diff_pieton,confort_pieton,access_pieton,type_voie
     *                   (mode==1) risque_velo,diff_velo,confort_velo,access_velo,type_voie
     *
     * @param mode 0 = pieton, 1 = velo
     */
    public static UpdateReport updateFromJson(InputStream jsonStream, int mode) throws Exception {
        if (jsonStream == null) throw new IllegalArgumentException("jsonStream is null");
        if (mode != 0 && mode != 1) throw new IllegalArgumentException("mode must be 0 (pieton) or 1 (velo)");

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        AtomicLong parsed = new AtomicLong(0);
        AtomicLong queued = new AtomicLong(0);
        AtomicLong skipBadId = new AtomicLong();
        AtomicLong skipMissingField = new AtomicLong();
        AtomicLong skipRange = new AtomicLong();
        AtomicLong skipType = new AtomicLong();
        AtomicLong skipAccess = new AtomicLong();
        long updated = 0;
        long notFound = 0;

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_LOGIN, DB_PASS)) {
            conn.setAutoCommit(false);

            // 1) TEMP TABLE：
            try (Statement st = conn.createStatement()) {
                st.execute(
                    "CREATE TEMP TABLE tmp_arc_update (" +
                        "osm_way_id BIGINT, " +
                        "from_node  BIGINT, " +
                        "to_node    BIGINT, " +
                        "risque     DOUBLE PRECISION, " +
                        "diff       DOUBLE PRECISION, " +
                        "confort    DOUBLE PRECISION, " +
                        "type_voie  INT, " +
                        "access     INT" +
                    ") ON COMMIT DROP"
                );
            }

            // 2) COPY：
            PipedInputStream inPipe = new PipedInputStream(64 * 1024);
            PipedOutputStream outPipe = new PipedOutputStream(inPipe);

            AtomicReference<Throwable> writerError = new AtomicReference<>(null);

            Thread writerThread = new Thread(() -> {
                try (
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outPipe, StandardCharsets.UTF_8));
                    JsonParser parser = factory.createParser(jsonStream)
                ) {

                    moveToItemsArray(parser);

                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        JsonNode item = mapper.readTree(parser);
                        if (item == null || item.isNull()) continue;

                        JsonNode props = item;
                    
                        parsed.incrementAndGet();
                        long p = parsed.get();
                        if (p % BATCH_HEARTBEAT == 0) {
                            System.out.println("[HEARTBEAT] parsed=" + p + " queued=" + queued.get());
                        }

                        
                        Double wayIdD   = getDouble(props, "osm_way_id", "osmWayId");
                        Double fromD    = getDouble(props, "from_node", "fromNode");
                        Double toD      = getDouble(props, "to_node", "toNode");

                        String wayId   = formatIdAsBigintText(wayIdD);
                        String fromId  = formatIdAsBigintText(fromD);
                        String toId    = formatIdAsBigintText(toD);

                        if (wayId == null || fromId == null || toId == null) {
                            skipBadId.incrementAndGet();
                        	continue; // 
                        }

                        
                        Double risque, diff, confort;
                        Integer access;
                        Integer typeVoie = 2;

                        if (mode == 0) {
                            risque  = getDouble(props, "risque_pieton", "risquePieton");
                            diff    = getDouble(props, "diff_pieton", "diffPieton");
                            confort = getDouble(props, "confort_pieton", "confortPieton");
                            access  = getInt(props, "access_pieton", "accessPieton");
                        } else {
                            risque  = getDouble(props, "risque_velo", "risqueVelo");
                            diff    = 1 - getDouble(props, "diff_velo", "diffVelo");
                            confort = getDouble(props, "confort_velo", "confortVelo");
                            access  = getInt(props, "access_velo", "accessVelo");
                        }

                        if (risque == null || diff == null || confort == null || typeVoie == null || access == null) {
                            skipMissingField.incrementAndGet();
                        	continue;
                        }

                        
                        if (!in01(risque) || !in01(diff) || !in01(confort)) {
                        	skipRange.incrementAndGet();
                        	continue;
                        }
                        if (!(typeVoie == 0 || typeVoie == 1 || typeVoie == 2)) {
                        	skipType.incrementAndGet();
                        	continue;}
                        if (mode == 0) {
                            if (!(access >= 0 && access <= 4)) { skipAccess.incrementAndGet();continue;}
                        } else {
                            if (!(access == 0 || access == 1))  { skipAccess.incrementAndGet();continue;}
                        }

                        
                        writer.write(wayId);
                        writer.write(',');
                        writer.write(fromId);
                        writer.write(',');
                        writer.write(toId);
                        writer.write(',');
                        writer.write(Double.toString(risque));
                        writer.write(',');
                        writer.write(Double.toString(diff));
                        writer.write(',');
                        writer.write(Double.toString(confort));
                        writer.write(',');
                        writer.write(Integer.toString(typeVoie));
                        writer.write(',');
                    
                        writer.write(Integer.toString(access));
                        writer.write('\n');

                        queued.incrementAndGet();
                    }
                } catch (Throwable t) {
                    writerError.set(t);
                } finally {
                    
                    try { outPipe.close(); } catch (IOException ignored) {}
                }
            }, "tmp-arc-update-writer");

            writerThread.start();

            
            PGConnection pgConn = conn.unwrap(PGConnection.class);
            CopyManager copyManager = pgConn.getCopyAPI();
            copyManager.copyIn(
                "COPY tmp_arc_update(osm_way_id, from_node, to_node, risque, diff, confort, type_voie, access) " +
                "FROM STDIN WITH (FORMAT csv)",
                inPipe
            );

            writerThread.join();
            System.out.println("skipBadId=" + skipBadId.get()
            + " skipMissingField=" + skipMissingField.get()
            + " skipRange=" + skipRange.get()
            + " skipType=" + skipType.get()
            + " skipAccess=" + skipAccess.get());
            try (Statement st = conn.createStatement();
            	     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tmp_arc_update")) {
            	    rs.next();
            	    System.out.println("tmp_arc_update rows=" + rs.getLong(1));
            	}
            if (writerError.get() != null) {
                throw new RuntimeException("Writer thread failed", writerError.get());
            }

            // 3) one-time UPDATE
            String updateSql;
            if (mode == 0) {
                updateSql =
                    "UPDATE routing_arc a SET " +
                    "  risque_pieton   = t.risque, " +
                    "  diff_pieton     = t.diff, " +
                    "  confort_pieton  = t.confort, " +
                    "  type_voie       = t.type_voie, " +
                    "  access_pieton   = t.access " +
                    "FROM tmp_arc_update t " +
                    "WHERE a.osm_way_id = t.osm_way_id " +
                    "  AND a.from_node  = t.from_node " +
                    "  AND a.to_node    = t.to_node";
            } else {
                updateSql =
                    "UPDATE routing_arc a SET " +
                    "  risque_velo    = t.risque, " +
                    "  diff_velo      = t.diff, " +
                    "  confort_velo   = t.confort, " +
                    "  type_voie      = t.type_voie, " +
                    "  access_velo    = t.access " +
                    "FROM tmp_arc_update t " +
                    "WHERE a.osm_way_id = t.osm_way_id " +
                    "  AND a.from_node  = t.from_node " +
                    "  AND a.to_node    = t.to_node";
            }

            try (Statement st = conn.createStatement()) {
                updated = st.executeUpdate(updateSql);
            }

            // 4) notFound
            String notFoundSql =
                "SELECT COUNT(*) " +
                "FROM tmp_arc_update t " +
                "LEFT JOIN routing_arc a " +
                "  ON a.osm_way_id = t.osm_way_id " +
                " AND a.from_node  = t.from_node " +
                " AND a.to_node    = t.to_node " +
                "WHERE a.osm_way_id IS NULL";

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(notFoundSql)) {
                if (rs.next()) notFound = rs.getLong(1);
            }

            conn.commit();
        }

        // skippedInvalid：
        long skipped = parsed.get() - queued.get();
        return new UpdateReport(parsed.get(), queued.get(), skipped, updated, notFound);
    }

    private static void moveToItemsArray(JsonParser parser) throws Exception {
        JsonToken first = parser.nextToken();
        if (first == null) throw new IllegalArgumentException("Empty JSON");

        if (first == JsonToken.START_ARRAY) {
            return; // already positioned at array start
        }

        if (first == JsonToken.START_OBJECT) {
            // scan to "features": [...]
            while (parser.nextToken() != null) {
                if (parser.currentToken() == JsonToken.FIELD_NAME && "features".equals(parser.getCurrentName())) {
                    JsonToken next = parser.nextToken();
                    if (next != JsonToken.START_ARRAY) {
                        throw new IllegalArgumentException("'features' must be an array, but got: " + next);
                    }
                    return;
                }
            }
            throw new IllegalArgumentException("Expected root array or FeatureCollection with 'features' array");
        }

        throw new IllegalArgumentException("Unsupported JSON root token: " + first);
    }

    private static Double getDouble(JsonNode props, String snake, String camel) {
        JsonNode n = props.get(snake);
        if (n == null) n = props.get(camel);
        if (n == null || n.isNull()) return null;
        if (!n.isNumber()) return null;
        return n.asDouble();
    }

    private static Integer getInt(JsonNode props, String snake, String camel) {
        JsonNode n = props.get(snake);
        if (n == null) n = props.get(camel);
        if (n == null || n.isNull()) return null;
        if (!n.isNumber()) return null;
        return n.asInt();
    }

    private static boolean in01(double x) {
        return Double.isFinite(x) && x >= 0.0 && x <= 1.0;
    }

    private static String formatIdAsBigintText(Double d) {
        if (d == null || !Double.isFinite(d)) return null;
        
        if (d <= 0) return null;

        double rounded = Math.rint(d); // nearest integer (ties to even)
        if (Math.abs(d - rounded) > 0.5) return null; 
        long v;
        try {
            
            v = (long) rounded;
        } catch (Exception e) {
            return null;
        }
        if (v <= 0) return null;
        return Long.toString(v);
    }

    public static final class UpdateReport {
        public final long parsedFeatures;
        public final long queuedUpdates;
        public final long skippedInvalid;
        public final long updatedRows;
        public final long notFound;

        public UpdateReport(long parsedFeatures, long queuedUpdates, long skippedInvalid, long updatedRows, long notFound) {
            this.parsedFeatures = parsedFeatures;
            this.queuedUpdates = queuedUpdates;
            this.skippedInvalid = skippedInvalid;
            this.updatedRows = updatedRows;
            this.notFound = notFound;
        }

        @Override
        public String toString() {
            return "UpdateReport{" +
                    "parsedFeatures=" + parsedFeatures +
                    ", queuedUpdates=" + queuedUpdates +
                    ", skippedInvalid=" + skippedInvalid +
                    ", updatedRows=" + updatedRows +
                    ", notFound=" + notFound +
                    '}';
        }
    }
}

