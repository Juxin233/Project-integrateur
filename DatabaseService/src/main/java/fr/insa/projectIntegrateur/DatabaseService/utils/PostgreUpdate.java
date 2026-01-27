package fr.insa.projectIntegrateur.DatabaseService.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public class PostgreUpdate {
	private static final String JDBC_URL = "jdbc:postgresql://srv-bdens:5432/bd3a_ng_60_base";
	private static final String DB_LOGIN ="bd3a_ng_60_log";
	private static final String DB_PASS = "etuu8Kee";

	    private static final int BATCH_SIZE = 5000;

	    // Only update the edges exist
	    private static final String UPDATE_ARC_SQL =
	            "UPDATE routing_arc SET " +
	            "  risque_pieton=?, risque_velo=?, " +
	            "  diff_pieton=?, diff_velo=?, " +
	            "  confort_pieton=?, confort_velo=?, " +
	            "  type_voie=? " +
	            "WHERE osm_way_id=? AND from_node=? AND to_node=?";

	    private PostgreUpdate() {}

	    /** update entry：transfer GeoJSON stream */
	    public static UpdateReport updateFromGeoJson(InputStream geoJsonStream) throws Exception {
	        if (geoJsonStream == null) {
	            throw new IllegalArgumentException("geoJsonStream is null");
	        }

	        ObjectMapper mapper = new ObjectMapper();
	        JsonFactory factory = mapper.getFactory();

	        long parsed = 0;
	        long queued = 0;
	        long skippedInvalid = 0;

	        long updatedRows = 0;
	        long notFound = 0;        

	        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_LOGIN, DB_PASS)) {
	            conn.setAutoCommit(false);

	            try (PreparedStatement ps = conn.prepareStatement(UPDATE_ARC_SQL);
	                 JsonParser parser = factory.createParser(geoJsonStream)) {

	                moveToFeaturesArray(parser);

	                int batch = 0;

	                while (parser.nextToken() != JsonToken.END_ARRAY) {
	                    JsonNode feature = mapper.readTree(parser);
	                    if (feature == null || !"Feature".equals(feature.path("type").asText())) continue;

	                    JsonNode geom = feature.get("geometry");
	                    JsonNode props = feature.get("properties");
	                    if (geom == null || props == null) continue;
	                    parsed++;

	                    long wayId = props.path("osm_way_id").asLong(0);
	                    long fromNode = props.path("from_node").asLong(0);
	                    long toNode = props.path("to_node").asLong(0);

	                    Double risqueP = getDouble(props, "risque_pieton", "risquePieton");
	                    Double risqueV = getDouble(props, "risque_velo", "risqueVelo");
	                    Double diffP   = getDouble(props, "diff_pieton", "diffPieton");
	                    Double diffV   = getDouble(props, "diff_velo", "diffVelo");
	                    Double confP   = getDouble(props, "confort_pieton", "confortPieton");
	                    Double confV   = getDouble(props, "confort_velo", "confortVelo");
	                    Integer typeVoie = getInt(props, "type_voie", "typeVoie");

	                    if (wayId == 0 || fromNode == 0 || toNode == 0 ||
	                        risqueP == null || risqueV == null || diffP == null || diffV == null ||
	                        confP == null || confV == null || typeVoie == null) {
	                        skippedInvalid++;
	                        continue;
	                    }

	                    if (!in01(risqueP) || !in01(risqueV) || !in01(diffP) || !in01(diffV) || !in01(confP) || !in01(confV)) {
	                        skippedInvalid++;
	                        continue;
	                    }
	                    if (!(typeVoie == 0 || typeVoie == 1 || typeVoie == 2)) {
	                        skippedInvalid++;
	                        continue;
	                    }

	                    // bind
	                    ps.setDouble(1, risqueP);
	                    ps.setDouble(2, risqueV);
	                    ps.setDouble(3, diffP);
	                    ps.setDouble(4, diffV);
	                    ps.setDouble(5, confP);
	                    ps.setDouble(6, confV);
	                    ps.setInt(7, typeVoie);

	                    ps.setLong(8, wayId);
	                    ps.setLong(9, fromNode);
	                    ps.setLong(10, toNode);

	                    ps.addBatch();
	                    queued++;
	                    batch++;

	                    if (batch >= BATCH_SIZE) {
	                        long[] res = executeAndCount(ps);
	                        for (long r : res) {
	                            if (r == 0) notFound++;
	                            else updatedRows += r;
	                        }
	                        conn.commit();
	                        batch = 0;
	                    }
	                }

	                if (batch > 0) {
	                    long[] res = executeAndCount(ps);
	                    for (long r : res) {
	                        if (r == 0) notFound++;
	                        else updatedRows += r;
	                    }
	                    conn.commit();
	                }
	            }
	        }

	        return new UpdateReport(parsed, queued, skippedInvalid, updatedRows, notFound);
	    }

	    private static long[] executeAndCount(PreparedStatement ps) throws SQLException {
	        int[] r = ps.executeBatch();
	        // JDBC: r[i] can be 0,1, or Statement.SUCCESS_NO_INFO(-2)
	        long[] out = new long[r.length];
	        for (int i = 0; i < r.length; i++) {
	            if (r[i] == Statement.SUCCESS_NO_INFO) out[i] = 1; 
	            else if (r[i] == Statement.EXECUTE_FAILED) out[i] = 0;
	            else out[i] = r[i];
	        }
	        return out;
	    }

	    private static void moveToFeaturesArray(JsonParser parser) throws Exception {
	        JsonToken tok = parser.nextToken();
	        if (tok == null) throw new IllegalArgumentException("Empty JSON");

	        while (parser.nextToken() != null) {
	            if (parser.currentToken() == JsonToken.FIELD_NAME && "features".equals(parser.getCurrentName())) {
	                parser.nextToken();
	                if (parser.currentToken() != JsonToken.START_ARRAY) {
	                    throw new IllegalArgumentException("'features' is not an array");
	                }
	                return;
	            }
	        }
	        throw new IllegalArgumentException("No 'features' array found");
	    }

	    private static boolean in01(double x) {
	        return Double.isFinite(x) && x >= 0.0 && x <= 1.0;
	    }

	    private static Double getDouble(JsonNode props, String snake, String camel) {
	        JsonNode n = props.get(snake);
	        if (n == null) n = props.get(camel);
	        if (n == null || n.isNull()) return null;
	        return n.asDouble();
	    }

	    private static Integer getInt(JsonNode props, String snake, String camel) {
	        JsonNode n = props.get(snake);
	        if (n == null) n = props.get(camel);
	        if (n == null || n.isNull()) return null;
	        return n.asInt();
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
