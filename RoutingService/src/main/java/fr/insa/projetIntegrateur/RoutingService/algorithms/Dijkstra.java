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
    public Reponse shortestPath(Graph g, long startId, long endId, int type) {
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
            return new Reponse(null,0,0,0,type,false);
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
            return new Reponse(null,0,0,0,type,false);
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
            
            List<Arc> arcs = g.getAdjacents(u);
//            System.out.println(arcs.size());
            if(arcs == null) continue;
            for (Arc arc : arcs) {
            	int T =arc.getType_route();
            	if (T != type && T !=2 ) continue; 
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
        LinkedList<Noeud> path = new LinkedList<>();
        Long cur = endId;
        double confort=0.0;
        double diff=0.0;
        double risque=0.0;
        double counter=0;
        while (prevArc.containsKey(cur)) {
            Arc arc = prevArc.get(cur); 
            path.addFirst(arc.getDestination());
            cur = arc.getOrigine().getId();
            switch (type) {
            	case 0:
            		confort += arc.getConfortPieton();
            		diff += arc.getConfortPieton();
            		risque += arc.getRisquePieton();
            		break;
            	case 1:
            		confort += arc.getConfortVelo();
            		diff += arc.getDiffVelo();
            		risque += arc.getRisqueVelo();
            		break;
            	case 2:
            		confort += arc.getConfortPieton();
            		diff += arc.getConfortPieton();
            		risque += arc.getRisquePieton();
            		break;
            }
            counter++;
        }
        path.addFirst(startNode);
        confort = confort / counter;
        diff = diff/counter;
        risque = risque/counter;
        System.out.println(confort +" "+diff +" "+risque);
        if (!prevArc.containsKey(endId) && startId != endId) {
            System.out.println("No path found between start and end!");
            return new Reponse(null,0,0,0,type,false);
        }

        return new Reponse(path,confort,diff,risque,type,false);
    }
}
