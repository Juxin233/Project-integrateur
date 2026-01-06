package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import fr.insa.projetIntegrateur.RoutingService.utils.Haversine;
import java.util.*;

public class ConstrainedAstar {

    /**
     * Calculates the shortest path using A* heuristic, but only traversing arcs 
     * that satisfy the user's security, comfort, and difficulty constraints.
     */
    public List<Arc> shortestPath(Graph graphe, long startId, long goalId, 
                                  double minSecurity, double minComfort, double minDifficulty) {

        Noeud start = graphe.getNoeud(startId);
        Noeud goal = graphe.getNoeud(goalId);

        if (start == null || goal == null) return Collections.emptyList();

        // Standard A* Maps
        Map<Long, Double> gScore = new HashMap<>(); // Cost from start
        Map<Long, Double> fScore = new HashMap<>(); // Cost from start + Heuristic to end
        Map<Long, Arc> cameFrom = new HashMap<>();  // To reconstruct path

        // Initialize scores
        for (Noeud n : graphe.getNoeuds()) {
            gScore.put(n.getId(), Double.POSITIVE_INFINITY);
            fScore.put(n.getId(), Double.POSITIVE_INFINITY);
        }

        gScore.put(startId, 0.0);
        fScore.put(startId, Haversine.distance(start.getLat(), start.getLon(), goal.getLat(), goal.getLon()));

        // PriorityQueue ordered by fScore (Estimation)
        PriorityQueue<Noeud> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> fScore.get(n.getId())));
        openSet.add(start);

        Set<Long> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            Noeud current = openSet.poll();

            if (current.getId() == goalId) {
                return reconstructPath(cameFrom, current);
            }

            closedSet.add(current.getId());

            for (Arc arc : Optional.ofNullable(graphe.getAdjacents(current.getId())).orElse(Collections.emptyList())) {
                Noeud neighbor = arc.getDestination();

                if (closedSet.contains(neighbor.getId())) continue;

                // --- CONSTRAINT CHECK ---
                // If the arc doesn't meet the user's range requirements, 
                // we treat it as if it doesn't exist (Invisible).
                if (!isArcValid(arc, minSecurity, minComfort, minDifficulty)) {
                    continue; 
                }
                // ------------------------

                double tentativeG = gScore.get(current.getId()) + arc.getLongueur(); // Scalar Cost = Distance

                if (tentativeG < gScore.get(neighbor.getId())) {
                    cameFrom.put(neighbor.getId(), arc);
                    gScore.put(neighbor.getId(), tentativeG);
                    
                    double h = Haversine.distance(neighbor.getLat(), neighbor.getLon(), goal.getLat(), goal.getLon());
                    fScore.put(neighbor.getId(), tentativeG + h);

                    // Add to PQ if not present (or re-add to update priority, usually handled by adding again)
                    openSet.add(neighbor);

                }
            }
        }

        return Collections.emptyList(); // No path found matching constraints
    }

    /**
 * Checks if the arc satisfies the range [0.0, 1.0 - (userValue - 0.05)]
 * for the Security indicator (since lower = safer),
 * and [userValue - 0.05, 1.0] for Comfort and Difficulty.
 */
    private boolean isArcValid(Arc arc, double userSec, double userConf, double userDiff) {
        double thresSec = Math.max(0.0, userSec - 0.05);
        double thresConf = Math.max(0.0, userConf - 0.05);
        double thresDiff = Math.max(0.0, userDiff - 0.05);

        // Security (Mapped to risquePieton: lower = safer)
        // Accept arcs only if risk ≤ (1.0 - threshold)
        if (arc.getRisquePieton() > (1.0 - thresSec)) return false;

        // Comfort
        if (arc.getConfortPieton() < thresConf || arc.getConfortPieton() > 1.0) return false;

        // Difficulty
        if (arc.getDiffPieton() < thresDiff || arc.getDiffPieton() > 1.0) return false;

        return true;
    }

    private List<Arc> reconstructPath(Map<Long, Arc> cameFrom, Noeud current) {
        LinkedList<Arc> path = new LinkedList<>();
        Long curId = current.getId();

        while (cameFrom.containsKey(curId)) {
            Arc arc = cameFrom.get(curId);
            path.addFirst(arc);
            curId = arc.getOrigine().getId();
        }
        return path;
    }
}