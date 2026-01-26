package fr.insa.projetIntegrateur.RoutingService.utils;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.model.Graph;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public final class PostgreLoader {
	private static final String JDBC_URL = "jdbc:postgresql://srv-bdens:5432/bd3a_ng_60_base";
	private static final String DB_LOGIN ="bd3a_ng_60_log";
	private static final String DB_PASS = "etuu8Kee";
	
    private PostgreLoader() { }

    /**
     * Read routing_node + routing_arc and generate Graph
     */
    public static Graph loadMap() throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_LOGIN, DB_PASS)) {
            // Read Only
            conn.setReadOnly(true);
            return loadMap(conn);
        }
    }

    private static Graph loadMap(Connection conn) throws SQLException {
        Graph g = new Graph();

        // 1) Load all nodes
        Map<Long, Noeud> nodeIndex = new HashMap<>(200_000);

        final String SQL_NODES = "SELECT osm_id, lat, lon FROM routing_node";
        try (PreparedStatement ps = conn.prepareStatement(SQL_NODES)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("osm_id");
                    double lat = rs.getDouble("lat");
                    double lon = rs.getDouble("lon");

                    Noeud n = new Noeud(id, lat, lon);
                    g.ajouterNoeud(n);
                    nodeIndex.put(id, n);
                }
            }
        }

        // 2) Load all arcs
        final String SQL_ARCS =
                "SELECT osm_way_id, from_node, to_node, length_m, " +
                "       type_voie, access_pieton, access_velo, risque_pieton, risque_velo, confort_pieton, confort_velo, diff_velo, diff_pieton " +
                "FROM routing_arc";

        try (PreparedStatement ps = conn.prepareStatement(SQL_ARCS)) {
            // ps.setFetchSize(10_000);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long arcId = rs.getLong("osm_way_id");
                    long fromId = rs.getLong("from_node");
                    long toId   = rs.getLong("to_node");

                    Noeud from = nodeIndex.get(fromId);
                    Noeud to   = nodeIndex.get(toId);

                    if (from == null || to == null) {
                        continue;
                    }

                    double length = rs.getDouble("length_m");

                    double risqueP = rs.getDouble("risque_pieton");
                    double risqueV = rs.getDouble("risque_velo");
                    double confortP = rs.getDouble("confort_pieton");
                    double confortV = rs.getDouble("confort_velo");
                    double diffV = rs.getDouble("diff_velo");
                    double diffP = rs.getDouble("diff_pieton");
                    
                    int typeVoie = rs.getInt("type_voie");
                    int accessPieton = rs.getInt("access_pieton");
                    int accessVelo = rs.getInt("access_velo");
                    Arc arc = new Arc(from, to, length, typeVoie,accessPieton,accessVelo, arcId,
                            risqueP, risqueV, confortP, confortV, diffP, diffV);
                    arc.setType_route(typeVoie);
                    g.ajouterArc(arc);
                }
            }
        }

        return g;
    }
	
	
}
