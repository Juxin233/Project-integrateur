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

    // Global variables (instance level)
    private boolean constraintsRelaxed = false;
    private double curSec;
    private double curConf;
    private double curDiff;

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
    public Reponse shortestPath(Graph graphe,
                                    long startId,
                                    long goalId,
                                    int type,
                                    double minSecurity,
                                    double minComfort,
                                    double minDifficulty) {

        if (graphe == null) return new Reponse(Collections.emptyList(),0,0,0,type,false);

        Noeud start = graphe.getNoeud(startId);
        Noeud goal = graphe.getNoeud(goalId);
        if (start == null || goal == null) return new Reponse(Collections.emptyList(),0,0,0,type,false);

        if (startId == goalId) {
            return new Reponse(Collections.singletonList(start),0,0,0,type,false);
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
                return reconstructPath(graphe, cameFrom, startId, goalId,type,constraintsRelaxed);
            }

            List<Arc> arcs = graphe.getAdjacents(cur.nodeId);
            if (arcs == null || arcs.isEmpty()) {
                continue;
            }

            // --- RELAXATION LOGIC START ---
            while (true) {
                boolean hasValidArc = false;
                for (Arc arc : arcs) {
                    if (arc == null) continue;
                    int T = arc.getType_route();
                    if (T != type && T != 2) continue;
                    // Check using global current vector
                    if (isArcValid(arc, type, this.curSec, this.curConf, this.curDiff)) {
                        hasValidArc = true;
                        break;
                    }
                }

                if (hasValidArc) break;

                // Stop if all criteria are zero
                if (this.curSec <= 0 && this.curConf <= 0 && this.curDiff <= 0) break;

                // Reduce non-zero components
                if (this.curSec > 0) this.curSec = Math.max(0.0, this.curSec - 0.05);
                if (this.curConf > 0) this.curConf = Math.max(0.0, this.curConf - 0.05);
                if (this.curDiff > 0) this.curDiff = Math.max(0.0, this.curDiff - 0.05);

                this.constraintsRelaxed = true;
            }
            // --- RELAXATION LOGIC END ---

            for (Arc arc : arcs) {
                if (arc == null) continue;
                int T = arc.getType_route();
                if (T != type && T != 2) continue;

                // Use global current vector
                if (!isArcValid(arc, type, this.curSec, this.curConf, this.curDiff)) {
                    continue;
                }

                double w = arc.getLongueur();
                if (!Double.isFinite(w)) continue;
                if (w < 0.0) throw new IllegalArgumentException("Negative arc length encountered: " + w);

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

        return new Reponse(Collections.emptyList(),0,0,0,type,this.constraintsRelaxed);
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
    private Reponse reconstructPath(Graph graphe, Map<Long, Arc> cameFrom, long startId, long goalId,int type,boolean change) {
        LinkedList<Noeud> path = new LinkedList<>();

        long currentId = goalId;
        Noeud current = graphe.getNoeud(currentId);
        if (current == null) return new Reponse(Collections.emptyList(),0,0,0,type,false);

        path.addFirst(current);
        double confort=0.0;
        double diff=0.0;
        double risque=0.0;
        double counter=0;
        while (currentId != startId) {
            Arc arc = cameFrom.get(currentId);
            if (arc == null) return new Reponse(Collections.emptyList(),0,0,0,type,false);

            Noeud prev = arc.getOrigine();
            if (prev == null) return new Reponse(Collections.emptyList(),0,0,0,type,false);

            currentId = prev.getId();
            path.addFirst(prev);
            switch (type) {
        	case 0:
        		confort += arc.getConfortPieton();
        		diff += arc.getDiffPieton();
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
            counter ++;
        }
        confort = confort / counter;
        diff = diff/counter;
        risque = risque/counter;

        return new Reponse(path,confort,diff,risque,type,change);
    }
}
