package fr.insa.projetIntegrateur.RoutingService.model;

import fr.insa.projetIntegrateur.RoutingService.utils.Haversine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private final Map<Long, Noeud> noeuds = new HashMap<>();
    private final Map<Long, List<Arc>> adj = new HashMap<>();

    public void ajouterNoeud(Noeud n) {
        noeuds.put(n.getId(), n);
        adj.putIfAbsent(n.getId(), new ArrayList<>());
    }

    public void ajouterArc(Arc arc) {
        adj.computeIfAbsent(arc.getOrigine().getId(), k -> new ArrayList<>()).add(arc);
    }

    public Noeud getNoeud(long id) {
        return noeuds.get(id);
    }

    public List<Arc> getAdjacents(long id) {
        return adj.getOrDefault(id, List.of());
    }

    public Collection<Noeud> getNoeuds() {
        return noeuds.values();
    }

    public int getNombreNoeuds() {
        return noeuds.size();
    }

    public int getNombreArcs() {
        return adj.values().stream().mapToInt(List::size).sum();
    }

    // --- NEW METHOD ---
    public long findNearestNode(double lat, double lon) {
        long nearestId = -1;
        double minDist = Double.MAX_VALUE;

        for (Noeud n : noeuds.values()) {
            double dist = Haversine.distance(lat, lon, n.getLat(), n.getLon());
            if (dist < minDist) {
                minDist = dist;
                nearestId = n.getId();
            }
        }
        return nearestId;
    }
}