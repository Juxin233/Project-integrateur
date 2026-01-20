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
	    private static final String UPDATE_PIETON_SQL =
	    	    "UPDATE routing_arc SET " +
	    	    "  risque_pieton=?, diff_pieton=?, confort_pieton=?, type_voie=? " +
	    	    "WHERE osm_way_id=? AND from_node=? AND to_node=?";

	    private static final String UPDATE_VELO_SQL =
	    	    "UPDATE routing_arc SET " +
	    	    "  risque_velo=?, diff_velo=?, confort_velo=?, type_voie=? " +
	    	    "WHERE osm_way_id=? AND from_node=? AND to_node=?";

	    private PostgreUpdate() {}

	    /** update entry：transfer GeoJSON stream */
	    public static UpdateReport updateFromJson(
	            InputStream jsonStream,
	            int mode
	    ) throws Exception {

	        ObjectMapper mapper = new ObjectMapper();
	        JsonFactory factory = mapper.getFactory();

	        long parsed = 0, queued = 0, skipped = 0, updated = 0, notFound = 0;

	        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_LOGIN, DB_PASS)) {
	            conn.setAutoCommit(false);

	            String sql = (mode == 0) ? UPDATE_PIETON_SQL : UPDATE_VELO_SQL;

	            try (PreparedStatement ps = conn.prepareStatement(sql);
	                 JsonParser parser = factory.createParser(jsonStream)) {

	                moveToItemsArray(parser); 

	                int batch = 0;

	                while (parser.nextToken() != JsonToken.END_ARRAY) {
	                    JsonNode node = mapper.readTree(parser);
	                    if (node == null) continue;
	                    parsed++;

	                    Double wayId   = getDouble(node, "osm_way_id", "osmWayId");
	                    Double fromNode= getDouble(node, "from_node", "fromNode");
	                    Double toNode  = getDouble(node, "to_node", "toNode");

	                    Double risque = getDouble(node, "risque_pieton", "risque_velo");
	                    Double diff   = getDouble(node, "diff_pieton", "diff_velo");
	                    Double conf   = getDouble(node, "confort_pieton", "confort_velo");
	                    Integer type  = getInt(node, "access_pieton", "access_velo");

	                    if (wayId==0 || fromNode==0 || toNode==0 ||
	                        risque==null || diff==null || conf==null || type==null) {
	                        skipped++;
	                        continue;
	                    }

	                    ps.setDouble(1, risque);
	                    ps.setDouble(2, diff);
	                    ps.setDouble(3, conf);
	                    ps.setInt(4, type);
	                    ps.setDouble(5, wayId);
	                    ps.setDouble(6, fromNode);
	                    ps.setDouble(7, toNode);

	                    ps.addBatch();
	                    queued++;
	                    batch++;

	                    if (batch >= BATCH_SIZE) {
	                        int[] r = ps.executeBatch();
	                        for (int x : r) {
	                            if (x == 0) notFound++;
	                            else updated++;
	                        }
	                        conn.commit();
	                        batch = 0;
	                    }
	                }

	                if (batch > 0) {
	                    int[] r = ps.executeBatch();
	                    for (int x : r) {
	                        if (x == 0) notFound++;
	                        else updated++;
	                    }
	                    conn.commit();
	                }
	            }
	        }

	        return new UpdateReport(parsed, queued, skipped, updated, notFound);
	    }



	    private static void moveToItemsArray(JsonParser parser) throws Exception {
	        JsonToken first = parser.nextToken();
	        if (first == null) {
	            throw new IllegalArgumentException("Empty JSON");
	        }
	        if (first != JsonToken.START_ARRAY) {
	            throw new IllegalArgumentException("Expected JSON array at root, but got: " + first);
	        }
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
