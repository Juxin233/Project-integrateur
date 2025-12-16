package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import java.util.*;


public class Dijkstra {

    private static class NodeDist implements Comparable<NodeDist> {
        long nodeId;
        double dist;
        NodeDist(long nodeId, double dist) {
            this.nodeId = nodeId;
            this.dist = dist;
        }
        @Override
        public int compareTo(NodeDist o) {
            return Double.compare(this.dist, o.dist);
        }
    }

    /**
     * Calcule le plus court chemin entre startId et endId dans le graphe g
     * @param g graphe
     * @param startId ID du noeud de départ
     * @param endId ID du noeud d'arrivée
     * @return liste des arcs formant le chemin le plus court, ou vide si pas de chemin
     */
    public List<Arc> shortestPath(Graph g, long startId, long endId) {
        // Map : noeud -> distance depuis start
        Map<Long, Double> dist = new HashMap<>();
        // Map : noeud -> arc précédent pour reconstruire le chemin
        Map<Long, Arc> prevArc = new HashMap<>();
        // Set des noeuds visités
        Set<Long> visited = new HashSet<>();
        Noeud startNode = g.getNoeud(startId);
        Noeud endNode = g.getNoeud(endId);
        if (startNode == null || endNode == null) {
            System.out.println("Start or end node does not exist in the graph!");
        }

        if (g.getAdjacents(startId).isEmpty()) {
            System.out.println("Start node has no outgoing arcs!");
        }
        if (g.getAdjacents(endId).isEmpty()) {
            System.out.println("End node has no outgoing arcs!");
        }

        
        // Initialisation
        for (Noeud n : g.getNoeuds()) {
            dist.put(n.getId(), Double.POSITIVE_INFINITY);
        }

        if (!dist.containsKey(startId) || !dist.containsKey(endId)) {
            System.out.println("Start or end node does not exist!");
            return Collections.emptyList();
        }

        dist.put(startId, 0.0);

        // PriorityQueue pour toujours choisir le noeud avec la distance minimale
        PriorityQueue<NodeDist> pq = new PriorityQueue<>();
        pq.add(new NodeDist(startId, 0.0));

        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            long u = current.nodeId;

            if (visited.contains(u)) continue;
            visited.add(u);

            if (u == endId) break; // arrivé au noeud final

            for (Arc arc : g.getAdjacents(u)) {
                long v = arc.getDestination().getId();
                double alt = dist.get(u) + arc.getLongueur();

                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    prevArc.put(v, arc);
                    pq.add(new NodeDist(v, alt));
                }
            }
        }

        // Reconstruire le chemin depuis endId
        LinkedList<Arc> path = new LinkedList<>();
        Long cur = endId;
        while (prevArc.containsKey(cur)) {
            Arc arc = prevArc.get(cur);
            path.addFirst(arc);
            cur = arc.getOrigine().getId();
        }

        if (path.isEmpty() && startId != endId) {
            System.out.println("No path found between start and end!");
        }

        return path;
    }
}