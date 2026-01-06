package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import java.util.*;

public class ConstrainedDijkstra {

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
     * Calculates the shortest path considering only arcs that meet the user's attribute requirements.
     *
     * @param g            The graph
     * @param startId      Start Node ID
     * @param endId        End Node ID
     * @param minSecurity  User preference for Security (0-1)
     * @param minComfort   User preference for Comfort (0-1)
     * @param minDifficulty User preference for Difficulty (0-1)
     * @return List of Arcs forming the path, or empty if no path exists.
     */
    public List<Arc> shortestPath(Graph g, long startId, long endId, 
                                  double minSecurity, double minComfort, double minDifficulty) {
        
        // 1. Setup Standard Dijkstra structures
        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Arc> prevArc = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        PriorityQueue<NodeDist> pq = new PriorityQueue<>();

        // 2. Initialize
        for (Noeud n : g.getNoeuds()) {
            dist.put(n.getId(), Double.POSITIVE_INFINITY);
        }
        dist.put(startId, 0.0);
        pq.add(new NodeDist(startId, 0.0));

        // 3. Main Loop
        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            long u = current.nodeId;

            if (visited.contains(u)) continue;
            visited.add(u);

            if (u == endId) break; 

            // 4. Explore Neighbors with FILTERING
            for (Arc arc : g.getAdjacents(u)) {
                
                // --- THE CONSTRAINT CHECK ---
                if (!isArcValid(arc, minSecurity, minComfort, minDifficulty)) {
                    continue; // Skip this arc, it doesn't meet user preferences
                }
                // ----------------------------

                long v = arc.getDestination().getId();
                double newDist = dist.get(u) + arc.getLongueur(); // Classic scalar cost (Distance)

                if (newDist < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, newDist);
                    prevArc.put(v, arc);
                    pq.add(new NodeDist(v, newDist));
                }
            }
        }

        // 5. Reconstruct Path
        return reconstructPath(prevArc, endId);
    }

    /**
     * Checks if the arc satisfies the range [UserValue - 0.05, 1.0] for all 3 indicators.
     */
    private boolean isArcValid(Arc arc, double userSec, double userConf, double userDiff) {
        // Calculate thresholds
        double thresSec = Math.max(0.0, userSec - 0.05);
        double thresConf = Math.max(0.0, userConf - 0.05);
        double thresDiff = Math.max(0.0, userDiff - 0.05);


        // Check Security (Mapped to risquePieton)
        double arcSec = arc.getRisquePieton(); 
        if (arcSec > (1.0 - thresSec)) return false;

        // Check Comfort
        double arcConf = arc.getConfortPieton();
        if (arcConf < thresConf || arcConf > 1.0) return false;

        // Check Difficulty
        double arcDiff = arc.getDiffPieton();
        if (arcDiff < thresDiff || arcDiff > 1.0) return false;

        return true;
    }

    private List<Arc> reconstructPath(Map<Long, Arc> prevArc, long endId) {
        LinkedList<Arc> path = new LinkedList<>();
        Long curr = endId;
        while (prevArc.containsKey(curr)) {
            Arc arc = prevArc.get(curr);
            path.addFirst(arc);
            curr = arc.getOrigine().getId();
        }
        return path;
    }
}