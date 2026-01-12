package fr.insa.projectIntegrateur.DatabaseService.service;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;

import java.io.InputStream;
import java.sql.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;


public class GeoJsonToPostgresImporter {
	private static final String JDBC_URL = "jdbc:postgresql://srv-bdens:5432/bd3a_ng_60_base";
	private static final String DB_LOGIN ="bd3a_ng_60_log";
	private static final String DB_PASS = "etuu8Kee";
	private static final String GEOJSON_MAP = "toulouse_graph_nodes_edges_area_Toulouse_2025-11-27.geojson";
	private static final double IND_MIN = 0.5;
    private static final double IND_MAX = 1.0;

    // Batch size for JDBC
    private static final int BATCH_SIZE = 5000;

    // Upsert SQL
    private static final String UPSERT_NODE_SQL =
            "INSERT INTO routing_node(osm_id, lon, lat) VALUES (?, ?, ?) " +
            "ON CONFLICT (osm_id) DO UPDATE SET lon = EXCLUDED.lon, lat = EXCLUDED.lat";

    private static final String UPSERT_ARC_SQL =
            "INSERT INTO routing_arc(" +
                    "osm_way_id, from_node, to_node, length_m, " +
                    "risque_pieton, risque_velo, diff_pieton, diff_velo, confort_pieton, confort_velo, " +
                    "type_voie" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (osm_way_id,from_node,to_node) DO UPDATE SET " +
                    "from_node=EXCLUDED.from_node, to_node=EXCLUDED.to_node, length_m=EXCLUDED.length_m, " +
                    "risque_pieton=EXCLUDED.risque_pieton, risque_velo=EXCLUDED.risque_velo, " +
                    "diff_pieton=EXCLUDED.diff_pieton, diff_velo=EXCLUDED.diff_velo, " +
                    "confort_pieton=EXCLUDED.confort_pieton, confort_velo=EXCLUDED.confort_velo, " +
                    "type_voie=EXCLUDED.type_voie";

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        Random rnd = new Random(42);

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_LOGIN, DB_PASS)) {
            conn.setAutoCommit(false);

            try (PreparedStatement psNode = conn.prepareStatement(UPSERT_NODE_SQL);
                 PreparedStatement psArc  = conn.prepareStatement(UPSERT_ARC_SQL)) {

                long nodeCount = 0;
                long arcCount = 0;
                long skippedMissing = 0;

                // =========================
                // PASS 1: upsert NODE
                // =========================
                try (InputStream is = GeoJsonToPostgresImporter.class.getClassLoader()
                        .getResourceAsStream(GEOJSON_MAP)) {

                    if (is == null) throw new IllegalStateException("GeoJSON not found in classpath");

                    JsonParser parser = factory.createParser(is);

                    moveToFeaturesArray(parser);

                    int nodeBatch = 0;

                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        JsonNode feature = mapper.readTree(parser);
                        if (feature == null || !"Feature".equals(feature.path("type").asText())) continue;

                        JsonNode geom = feature.get("geometry");
                        JsonNode props = feature.get("properties");
                        if (geom == null || props == null) continue;

                        if (!"Point".equals(geom.path("type").asText(""))) continue;

                        long osmId = props.path("osm_id").asLong(0);
                        JsonNode coords = geom.get("coordinates");
                        if (osmId == 0 || coords == null || coords.size() < 2) continue;

                        double lon = coords.get(0).asDouble();
                        double lat = coords.get(1).asDouble();

                        psNode.setLong(1, osmId);
                        psNode.setDouble(2, lon);
                        psNode.setDouble(3, lat);
                        psNode.addBatch();

                        nodeCount++;
                        nodeBatch++;

                        if (nodeBatch >= BATCH_SIZE) {
                            psNode.executeBatch();
                            nodeBatch = 0;
                        }
                    }

                    if (nodeBatch > 0) psNode.executeBatch();
                }

                conn.commit();
                System.out.println("Nodes upserted: " + nodeCount);

                Set<Long> existingNodeIds = new HashSet<>(2_000_000);
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT osm_id FROM routing_node")) {
                    while (rs.next()) {
                        existingNodeIds.add(rs.getLong(1));
                    }
                }
                System.out.println("Nodes loaded into memory for FK check: " + existingNodeIds.size());

                // =========================
                // PASS 2: upsert ARC
                // =========================
                try (InputStream is = GeoJsonToPostgresImporter.class.getClassLoader()
                        .getResourceAsStream(GEOJSON_MAP)) {

                    if (is == null) throw new IllegalStateException("GeoJSON not found in classpath");

                    JsonParser parser = factory.createParser(is);

                    moveToFeaturesArray(parser);

                    int arcBatch = 0;

                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        JsonNode feature = mapper.readTree(parser);
                        if (feature == null || !"Feature".equals(feature.path("type").asText())) continue;

                        JsonNode geom = feature.get("geometry");
                        JsonNode props = feature.get("properties");
                        if (geom == null || props == null) continue;

                        if (!"LineString".equals(geom.path("type").asText(""))) continue;

                        long wayId = props.path("osm_way_id").asLong(0);
                        long fromNode = props.path("from_node").asLong(0);
                        long toNode = props.path("to_node").asLong(0);
                        String onewayVal = props.has("oneway") ? props.get("oneway").asText() : "no";
                        
                        JsonNode coords = geom.get("coordinates");
                        if (wayId == 0 || fromNode == 0 || toNode == 0 || coords == null || coords.size() < 2) continue;

                        // === NEW ===：node not exist pass
                        if (!existingNodeIds.contains(fromNode) || !existingNodeIds.contains(toNode)) {
                            skippedMissing++;
                            continue;
                        }

                        double lengthM = linestringLengthMeters(coords);

                        double risqueP = randRange(rnd, IND_MIN, IND_MAX);
                        double risqueV = randRange(rnd, IND_MIN, IND_MAX);
                        double diffP   = randRange(rnd, IND_MIN, IND_MAX);
                        double diffV   = randRange(rnd, IND_MIN, IND_MAX);
                        double confP   = randRange(rnd, IND_MIN, IND_MAX);
                        double confV   = randRange(rnd, IND_MIN, IND_MAX);

                        int typeVoie = inferTypeVoie(props);
                        
                        
                        psArc.setLong(1, wayId);
                        psArc.setLong(2, fromNode);
                        psArc.setLong(3, toNode);
                        psArc.setDouble(4, lengthM);
                        psArc.setDouble(5, risqueP);
                        psArc.setDouble(6, risqueV);
                        psArc.setDouble(7, diffP);
                        psArc.setDouble(8, diffV);
                        psArc.setDouble(9, confP);
                        psArc.setDouble(10, confV);
                        psArc.setInt(11, typeVoie);
                        
                        switch (onewayVal) {
                        	case "yes" -> {   
                        		psArc.setLong(2, fromNode);
                        		psArc.setLong(3, toNode);
                        		psArc.addBatch();
                                arcCount++;
                                arcBatch++;
                        		break;}
                        case "no" -> {   
                    		psArc.setLong(2, fromNode);
                    		psArc.setLong(3, toNode);
                    		psArc.addBatch();
                    		psArc.setLong(3, fromNode);
                    		psArc.setLong(2, toNode);
                    		psArc.addBatch();
                            arcCount+=2;
                            arcBatch+=2;
                    		break;}
                        case "-1" -> {   
                    		psArc.setLong(2, fromNode);
                    		psArc.setLong(3, toNode);
                    		psArc.addBatch();
                            arcCount++;
                            arcBatch++;
                    		break;}
                        default -> {   
                    		psArc.setLong(2, fromNode);
                    		psArc.setLong(3, toNode);
                    		psArc.addBatch();
                    		psArc.setLong(3, fromNode);
                    		psArc.setLong(2, toNode);
                    		psArc.addBatch();
                            arcCount+=2;
                            arcBatch+=2;
                    		break;}
                        }

                        if (arcBatch >= BATCH_SIZE) {
                            psArc.executeBatch();
                            arcBatch = 0;
                        }
                    }

                    if (arcBatch > 0) psArc.executeBatch();
                }

                conn.commit();
                System.out.println("Arcs upserted: " + arcCount);
                System.out.println("Arcs skipped (missing endpoint node): " + skippedMissing);
            }
        }
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

    private static double randRange(Random rnd, double a, double b) {
        return a + (b - a) * rnd.nextDouble();
    }

    /**
     * Best-effort inference:
     * - cycleway / path (bicycle designated) => 1 (velo)
     * - footway / pedestrian / steps => 0 (pieton)
     * - otherwise => 2 (both)
     *
     * If you later define exact business rules, change this function only.
     */
    private static int inferTypeVoie(JsonNode props) {
        String highway = props.path("highway").asText("").toLowerCase(Locale.ROOT);
        String bicycle = props.path("bicycle").asText("").toLowerCase(Locale.ROOT);
        String foot    = props.path("foot").asText("").toLowerCase(Locale.ROOT);

        // Explicit tags first
        if ("designated".equals(bicycle) && !"no".equals(foot)) return 1;
        if ("designated".equals(foot) && !"no".equals(bicycle)) return 0;

        // Highway-based guess
        if (highway.contains("cycleway")) return 1;
        if (highway.contains("footway") || highway.contains("pedestrian") || highway.contains("steps")) return 0;

        // Default both
        return 2;
    }

    /**
     * coords is an array like: [[lon,lat],[lon,lat],...]
     */
    private static double linestringLengthMeters(JsonNode coords) {
        double total = 0.0;
        for (int i = 1; i < coords.size(); i++) {
            JsonNode p0 = coords.get(i - 1);
            JsonNode p1 = coords.get(i);
            if (p0 == null || p1 == null || p0.size() < 2 || p1.size() < 2) continue;

            double lon0 = p0.get(0).asDouble();
            double lat0 = p0.get(1).asDouble();
            double lon1 = p1.get(0).asDouble();
            double lat1 = p1.get(1).asDouble();

            total += haversineMeters(lat0, lon0, lat1, lon1);
        }
        return total;
    }

    /**
     * Haversine in meters.
     */
    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

}
