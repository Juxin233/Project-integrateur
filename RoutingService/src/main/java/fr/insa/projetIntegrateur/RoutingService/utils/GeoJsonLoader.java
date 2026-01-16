package fr.insa.projetIntegrateur.RoutingService.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.model.Graph;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

public class GeoJsonLoader {

    private final ObjectMapper mapper = new ObjectMapper();
    // arc unique ID
    private final AtomicLong arcIdGen = new AtomicLong(1);

    private final Random random = new Random();

    private double randIndicator() {
        return 0.5 + random.nextDouble() * 0.5;
    }

    private Noeud getOrCreateOsmNode(Graph g,
                                    Map<Long, Noeud> osmCache,
                                    long osmId,
                                    double lat,
                                    double lon) {
        Noeud existing = osmCache.get(osmId);
        if (existing != null) return existing;
        Noeud nn = new Noeud(osmId, lat, lon);
        g.ajouterNoeud(nn);
        osmCache.put(osmId, nn);
        return nn;
    }

    public Graph charger(String resource) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (is == null) throw new RuntimeException("Fichier GeoJSON introuvable : " + resource);

            JsonNode root = mapper.readTree(is);
            JsonNode features = root.get("features");
            if (features == null || !features.isArray()) {
                throw new RuntimeException("GeoJSON invalide : 'features' manquant ou non tableau.");
            }

            Graph g = new Graph();

            // 1) OSM node id -> Noeud
            Map<Long, Noeud> osmCache = new HashMap<>();

            // -------- Pass 1: load Point features (real OSM node) --------
            for (JsonNode feature : features) {
                JsonNode geom = feature.get("geometry");
                JsonNode props = feature.get("properties");
                if (geom == null || props == null) continue;

                if (!"Point".equals(geom.path("type").asText(""))) continue;
                if (!props.has("osm_id")) continue;

                JsonNode coords = geom.get("coordinates");
                if (coords == null || coords.size() < 2) continue;

                long osmId = props.get("osm_id").asLong();
                double lon = coords.get(0).asDouble();
                double lat = coords.get(1).asDouble();

                Noeud n = new Noeud(osmId, lat, lon);
                g.ajouterNoeud(n);
                osmCache.put(osmId, n);
            }


            // -------- Pass 2: load LineString features  --------
            for (JsonNode feature : features) {
                JsonNode geom = feature.get("geometry");
                JsonNode props = feature.get("properties");
                if (geom == null || props == null) continue;

                if (!"LineString".equals(geom.path("type").asText(""))) continue;

                JsonNode coords = geom.get("coordinates");
                if (coords == null || coords.size() < 2) continue;

                if (!props.has("from_node") || !props.has("to_node")) continue;

                long fromOsm = props.get("from_node").asLong();
                long toOsm = props.get("to_node").asLong();
                long arcId = props.get("osm_way_id").asLong();
                String typeVoie = props.has("highway") ? props.get("highway").asText() : null;
                String onewayVal = props.has("oneway") ? props.get("oneway").asText() : "no";

                int n = coords.size();
                double dist=0.0;
                for (int i =0;i < n-1;i++) {
                	double lonCurrent = coords.get(i).get(0).asDouble();
                    double latCurrent = coords.get(i).get(1).asDouble();
                    double lonNext = coords.get(i+1).get(0).asDouble();
                    double latNext = coords.get(i+1).get(1).asDouble();
                    dist += Haversine.distance(latCurrent, lonCurrent, latNext, lonNext);
                }
                double lonFirst = coords.get(0).get(0).asDouble();
                double latFirst = coords.get(0).get(1).asDouble();
                double lonLast = coords.get(n - 1).get(0).asDouble();
                double latLast = coords.get(n - 1).get(1).asDouble();

                Noeud fromNode = getOrCreateOsmNode(g, osmCache,  fromOsm, latFirst, lonFirst);
                Noeud toNode = getOrCreateOsmNode(g, osmCache, toOsm, latLast, lonLast);

                // basuler  from/to nodes；ustiliser d'abord OSM id, sinon synthetic id
                Noeud a = fromNode;
                Noeud b = toNode;

                double risquePieton = randIndicator();
                double risqueVelo = randIndicator();
                double confortPieton = randIndicator();
                double confortVelo = randIndicator();
                double diffVelo = randIndicator();
                double diffPieton = randIndicator();

                switch (onewayVal) {
                    case "yes" -> g.ajouterArc(new Arc(a, b, dist, typeVoie, arcId,
                            risquePieton, risqueVelo, confortPieton, confortVelo, diffVelo, diffPieton));
                    case "no" -> {
                        g.ajouterArc(new Arc(a, b, dist, typeVoie, arcId,
                                risquePieton, risqueVelo, confortPieton, confortVelo, diffVelo, diffPieton));
                        g.ajouterArc(new Arc(b, a, dist, typeVoie, arcId,
                                risquePieton, risqueVelo, confortPieton, confortVelo, diffVelo, diffPieton));
                    }
                    case "-1" -> g.ajouterArc(new Arc(b, a, dist, typeVoie, arcId,
                            risquePieton, risqueVelo, confortPieton, confortVelo, diffVelo, diffPieton));
                    default -> {
                        g.ajouterArc(new Arc(a, b, dist, typeVoie, arcId,
                                risquePieton, risqueVelo, confortPieton, confortVelo, diffVelo, diffPieton));
                        g.ajouterArc(new Arc(b, a, dist, typeVoie, arcId,
                                risquePieton, risqueVelo, confortPieton, confortVelo, diffVelo, diffPieton));
                    }
                }
            }
            
            System.out.println("Graphe chargé : " + g.getNombreNoeuds() + " noeuds, " + g.getNombreArcs() + " arcs.");
            return g;
        }
    }
}






