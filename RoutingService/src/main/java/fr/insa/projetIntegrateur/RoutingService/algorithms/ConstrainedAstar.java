package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import fr.insa.projetIntegrateur.RoutingService.service.PathService.PathResult; // Import wrapper
import fr.insa.projetIntegrateur.RoutingService.utils.Haversine;

import java.util.*;

public class ConstrainedAstar {

    private static final double TOL = 0.05;

    // Global variables (instance level)
    private boolean constraintsRelaxed = false;
    private double curSec;
    private double curConf;
    private double curDiff;

    private static final class QEntry {
        final long nodeId;
        final double g;
        final double f;

        QEntry(long nodeId, double g, double f) {
            this.nodeId = nodeId;
            this.g = g;
            this.f = f;
        }
    }

    public PathResult shortestPath(Graph graphe,
                                   long startId,
                                   long goalId,
                                   int type,
                                   double minSecurity,
                                   double minComfort,
                                   double minDifficulty) {

        // Init globals
        this.constraintsRelaxed = false;
        this.curSec = minSecurity;
        this.curConf = minComfort;
        this.curDiff = minDifficulty;

        if (graphe == null) return new PathResult(Collections.emptyList(), false);

        Noeud start = graphe.getNoeud(startId);
        Noeud goal = graphe.getNoeud(goalId);
        if (start == null || goal == null) return new PathResult(Collections.emptyList(), false);

        if (startId == goalId) {
            return new PathResult(Collections.singletonList(start), false);
        }

        Map<Long, Double> gScore = new HashMap<>();
        gScore.put(startId, 0.0);

        Map<Long, Arc> cameFrom = new HashMap<>();

        PriorityQueue<QEntry> openSet = new PriorityQueue<>(Comparator.comparingDouble(e -> e.f));
        openSet.add(new QEntry(startId, 0.0, heuristic(start, goal)));

        while (!openSet.isEmpty()) {
            QEntry cur = openSet.poll();

            double bestKnownG = gScore.getOrDefault(cur.nodeId, Double.POSITIVE_INFINITY);
            if (cur.g != bestKnownG) {
                continue;
            }

            if (cur.nodeId == goalId) {
                List<Noeud> path = reconstructPath(graphe, cameFrom, startId, goalId);
                return new PathResult(path, this.constraintsRelaxed);
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

        return new PathResult(Collections.emptyList(), this.constraintsRelaxed);
    }

    private double heuristic(Noeud node, Noeud goal) {
        double h = Haversine.distance(node.getLat(), node.getLon(), goal.getLat(), goal.getLon());
        if (!Double.isFinite(h) || h < 0.0) return 0.0;
        return h;
    }

    private boolean isArcValid(Arc arc, int type, double minSecurity, double minComfort, double minDifficulty) {
        double thresSec = clamp01(minSecurity - TOL);
        double thresConf = clamp01(minComfort - TOL);
        double thresDiff = clamp01(minDifficulty - TOL);
        double arcSec = 0.0;
        double arcConf = 0.0;
        double arcDiff = 0.0;
        
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

    private List<Noeud> reconstructPath(Graph graphe, Map<Long, Arc> cameFrom, long startId, long goalId) {
        LinkedList<Noeud> path = new LinkedList<>();

        long currentId = goalId;
        Noeud current = graphe.getNoeud(currentId);
        if (current == null) return Collections.emptyList();

        path.addFirst(current);

        while (currentId != startId) {
            Arc arc = cameFrom.get(currentId);
            if (arc == null) return Collections.emptyList();
            Noeud prev = arc.getOrigine();
            if (prev == null) return Collections.emptyList();

            currentId = prev.getId();
            path.addFirst(prev);
        }

        return path;
    }
}