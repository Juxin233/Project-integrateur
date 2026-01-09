package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import fr.insa.projetIntegrateur.RoutingService.utils.Haversine;

import java.util.*;

/**
 * A* with constraints (security/comfort/difficulty filters).
 *
 * Robust version:
 * - Does NOT rely on PriorityQueue re-heapify on external mutable maps.
 * - Uses immutable queue entries + lazy skipping of outdated entries.
 * - Defensive checks for null nodes, null adjacency lists, NaN/Inf, negative lengths.
 * - Allows "re-open" naturally (no hard closedSet), so it is safe even if heuristic is not consistent.
 */
public class ConstrainedAstar {

    /** Small tolerance used in your project logic. */
    private static final double TOL = 0.05;

    /** Priority queue entry (immutable). */
    private static final class QEntry {
        final long nodeId;
        final double g;   // best known cost from start at the time of insertion
        final double f;   // g + h

        QEntry(long nodeId, double g, double f) {
            this.nodeId = nodeId;
            this.g = g;
            this.f = f;
        }
    }

    /**
     * Calculates the shortest path using A* heuristic, but only traversing arcs
     * that satisfy the user's security, comfort, and difficulty constraints.
     *
     * @return List of nodes from start to goal (inclusive) or empty list if no path.
     */
    public List<Noeud> shortestPath(Graph graphe,
                                    long startId,
                                    long goalId,
                                    int type,
                                    double minSecurity,
                                    double minComfort,
                                    double minDifficulty) {

        if (graphe == null) return Collections.emptyList();

        Noeud start = graphe.getNoeud(startId);
        Noeud goal = graphe.getNoeud(goalId);
        if (start == null || goal == null) return Collections.emptyList();

        if (startId == goalId) {
            return Collections.singletonList(start);
        }

        // gScore: best known cost from start to each nodeId
        Map<Long, Double> gScore = new HashMap<>();
        gScore.put(startId, 0.0);

        // cameFrom: for each nodeId, store the arc used to reach it with best gScore
        Map<Long, Arc> cameFrom = new HashMap<>();

        // Open set: ordered by smallest f
        PriorityQueue<QEntry> openSet = new PriorityQueue<>(Comparator.comparingDouble(e -> e.f));
        openSet.add(new QEntry(startId, 0.0, heuristic(start, goal)));

        while (!openSet.isEmpty()) {
            QEntry cur = openSet.poll();

            // Lazy skip: if this entry is not the current best g for node, discard it.
            double bestKnownG = gScore.getOrDefault(cur.nodeId, Double.POSITIVE_INFINITY);
            if (cur.g != bestKnownG) {
                continue;
            }

            if (cur.nodeId == goalId) {
                return reconstructPath(graphe, cameFrom, startId, goalId);
            }

            List<Arc> arcs = graphe.getAdjacents(cur.nodeId);
            if (arcs == null || arcs.isEmpty()) {
                continue;
            }

            for (Arc arc : arcs) {
                if (arc == null) continue;
            	int T =arc.getType_route();
            	if (T != type && T !=2 ) continue; 
                if (!isArcValid(arc,type, minSecurity, minComfort, minDifficulty)) {
                    continue;
                }

                double w = arc.getLongueur();
                if (!Double.isFinite(w)) {
                    // Invalid weight; skip to avoid NaN poisoning
                    continue;
                }
                if (w < 0.0) {
                    // Dijkstra/A* require non-negative weights for correctness
                    throw new IllegalArgumentException("Negative arc length encountered: " + w);
                }

                Noeud to = arc.getDestination();
                if (to == null) continue;

                long v = to.getId();
                double tentativeG = cur.g + w;

                double oldG = gScore.getOrDefault(v, Double.POSITIVE_INFINITY);
                if (tentativeG < oldG) {
                    gScore.put(v, tentativeG);
                    cameFrom.put(v, arc);

                    double f = tentativeG + heuristic(to, goal);
                    openSet.add(new QEntry(v, tentativeG, f));
                }
            }
        }

        return Collections.emptyList();
    }

    /** Heuristic: great-circle distance (meters-like) between node and goal. */
    private double heuristic(Noeud node, Noeud goal) {
        double h = Haversine.distance(node.getLat(), node.getLon(), goal.getLat(), goal.getLon());
        if (!Double.isFinite(h) || h < 0.0) return 0.0;
        return h;
    }

    /**
     * Validates an arc against user requirements.
     *
     * IMPORTANT (based on your tests): values are treated as "higher is better".
     * - arcSecurity must be >= (minSecurity - 0.05)
     * - arcComfort  must be >= (minComfort  - 0.05)
     * - arcDifficulty must be >= (minDifficulty - 0.05)
     *
     * Also enforces that attributes are finite and within [0, 1].
     */
    private boolean isArcValid(Arc arc, int type,double minSecurity, double minComfort, double minDifficulty) {
        double thresSec = clamp01(minSecurity - TOL);
        double thresConf = clamp01(minComfort - TOL);
        double thresDiff = clamp01(minDifficulty - TOL);
        double arcSec =0.0;
        double arcConf =0.0;
        double arcDiff =0.0;
        
        switch (type) {
	        case 0:
	            arcSec = arc.getRisquePieton();
	            arcConf = arc.getConfortPieton();
	            arcDiff = arc.getDiffPieton();
	            break;
	        case 1:
	            arcSec = arc.getRisqueVelo();
	            arcConf = arc.getConfortVelo();
	            arcDiff = arc.getDiffVelo();
	            break;
            default:
                arcSec = arc.getRisquePieton();
                arcConf = arc.getConfortPieton();
                arcDiff = arc.getDiffPieton();
        }
        	
        if (!isFinite01(arcSec) || !isFinite01(arcConf) || !isFinite01(arcDiff)) {
            return false;
        }

        return arcSec >= thresSec && arcConf >= thresConf && arcDiff >= thresDiff;
    }

    private static boolean isFinite01(double x) {
        return Double.isFinite(x) && x >= 0.0 && x <= 1.0;
    }

    private static double clamp01(double x) {
        if (!Double.isFinite(x)) return 0.0;
        if (x < 0.0) return 0.0;
        if (x > 1.0) return 1.0;
        return x;
    }

    /**
     * Reconstructs node path from startId to goalId using cameFrom map (nodeId -> arc used).
     * Returns empty list if reconstruction fails (should not happen when algorithm is correct).
     */
    private List<Noeud> reconstructPath(Graph graphe, Map<Long, Arc> cameFrom, long startId, long goalId) {
        LinkedList<Noeud> path = new LinkedList<>();

        long currentId = goalId;
        Noeud current = graphe.getNoeud(currentId);
        if (current == null) return Collections.emptyList();

        path.addFirst(current);

        while (currentId != startId) {
            Arc arc = cameFrom.get(currentId);
            if (arc == null) {
                // No predecessor => cannot reconstruct
                return Collections.emptyList();
            }
            Noeud prev = arc.getOrigine();
            if (prev == null) return Collections.emptyList();

            currentId = prev.getId();
            path.addFirst(prev);
        }

        return path;
    }
}
