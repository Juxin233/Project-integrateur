package fr.insa.projetIntegrateur.RoutingService.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.model.Graph;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GeoJsonLoader {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicLong generatedNodeId = new AtomicLong(1);

    /**
     * Charge un GeoJSON en créant un graphe avec arcs bidirectionnels.
     * @param resource Chemin du fichier dans src/main/resources
     * @return Graphe prêt pour Dijkstra / A*
     * @throws Exception
     */
    public Graph charger(String resource) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
        if (is == null) {
            throw new RuntimeException("Fichier GeoJSON introuvable : " + resource);
        }

        JsonNode root = mapper.readTree(is);
        Graph graphe = new Graph();
        Map<String, Noeud> pointCache = new HashMap<>();

        // 1️⃣ Première passe : créer tous les Noeuds de type Point
        for (JsonNode feature : root.get("features")) {
            String geomType = feature.get("geometry").get("type").asText();
            if ("Point".equals(geomType)) {
                JsonNode coords = feature.get("geometry").get("coordinates");
                JsonNode props = feature.get("properties");
                long id = props.get("osm_id").asLong();
                double lon = coords.get(0).asDouble();
                double lat = coords.get(1).asDouble();
                Noeud n = new Noeud(id, lat, lon);
                graphe.ajouterNoeud(n);
                pointCache.put(lat + "," + lon, n);
            }
        }

        // 2️⃣ Deuxième passe : traiter LineString et créer arcs bidirectionnels
        for (JsonNode feature : root.get("features")) {
            String geomType = feature.get("geometry").get("type").asText();
            if (!"LineString".equals(geomType)) continue;

            JsonNode coords = feature.get("geometry").get("coordinates");
            JsonNode props = feature.get("properties");
            if (coords.size() < 2) continue;

            for (int i = 0; i < coords.size() - 1; i++) {
                double lon1 = coords.get(i).get(0).asDouble();
                double lat1 = coords.get(i).get(1).asDouble();
                double lon2 = coords.get(i + 1).get(0).asDouble();
                double lat2 = coords.get(i + 1).get(1).asDouble();

                // récupérer ou créer les noeuds
                Noeud n1 = pointCache.computeIfAbsent(lat1 + "," + lon1, k -> {
                    long newId = generatedNodeId.getAndIncrement();
                    Noeud nn = new Noeud(newId, lat1, lon1);
                    graphe.ajouterNoeud(nn);
                    return nn;
                });
                Noeud n2 = pointCache.computeIfAbsent(lat2 + "," + lon2, k -> {
                    long newId = generatedNodeId.getAndIncrement();
                    Noeud nn = new Noeud(newId, lat2, lon2);
                    graphe.ajouterNoeud(nn);
                    return nn;
                });

                // distance
                double dist = Haversine.distance(n1.getLat(), n1.getLon(), n2.getLat(), n2.getLon());
                String typeVoie = props.has("highway") ? props.get("highway").asText() : null;

                // arcs bidirectionnels
                graphe.ajouterArc(new Arc(n1, n2, dist, typeVoie));
                graphe.ajouterArc(new Arc(n2, n1, dist, typeVoie));
            }
        }

        System.out.println("Graphe chargé : " + graphe.getNoeuds().size() + " noeuds, " 
                           + graphe.getNombreArcs() + " arcs.");
        return graphe;
    }
}