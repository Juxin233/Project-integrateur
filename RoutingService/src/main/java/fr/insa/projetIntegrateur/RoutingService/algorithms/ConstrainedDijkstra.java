package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import java.util.*;

public class ConstrainedDijkstra {
    private static final double TOL = 0.05;

    private static class NodeDist implements Comparable<NodeDist> {
        final long nodeId;
        final double dist;

        NodeDist(long nodeId, double dist) {
            this.nodeId = nodeId;
            this.dist = dist;
        }

        @Override
        public int compareTo(NodeDist o) {
            return Double.compare(this.dist, o.dist);
        }
    }

    public List<Noeud> shortestPath(Graph g,
                                    long startId,
                                    long endId,
                                    int type,
                                    double minSecurity,
                                    double minComfort,
                                    double minDifficulty) {

        if (g == null) return Collections.emptyList();
        Noeud start = g.getNoeud(startId);
        Noeud goal = g.getNoeud(endId);
        if (start == null || goal == null) return Collections.emptyList();
        if (startId == endId) return Collections.singletonList(start);

        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Arc> prevArc = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        PriorityQueue<NodeDist> pq = new PriorityQueue<>();

        // init (keep your style)
        for (Noeud n : g.getNoeuds()) {
            dist.put(n.getId(), Double.POSITIVE_INFINITY);
        }
        dist.put(startId, 0.0);
        pq.add(new NodeDist(startId, 0.0));

        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            long u = current.nodeId;

            if (visited.contains(u)) continue;
            visited.add(u);

            if (u == endId) break;

            List<Arc> adj = g.getAdjacents(u);
            if (adj == null || adj.isEmpty()) continue;

            double du = dist.getOrDefault(u, Double.POSITIVE_INFINITY);

            for (Arc arc : adj) {
                if (arc == null) continue;

                int roadType = arc.getType_route();
                if (roadType != type && roadType != 2) continue;

                if (!isArcValid(arc, type, minSecurity, minComfort, minDifficulty)) continue;

                Noeud dest = arc.getDestination();
                if (dest == null) continue;

                double w = arc.getLongueur();
                if (!Double.isFinite(w)) continue;
                if (w < 0.0) throw new IllegalArgumentException("Negative arc length: " + w);

                long v = dest.getId();
                double nd = du + w;

                if (nd < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, nd);
                    prevArc.put(v, arc);
                    pq.add(new NodeDist(v, nd));
                }
            }
        }

        // If unreachable, return empty (instead of attempting reconstruction)
        if (!prevArc.containsKey(endId)) return Collections.emptyList();

        return reconstructPath(g, prevArc, startId, endId);
    }

    private boolean isArcValid(Arc arc,
                              int type,
                              double minSecurity,
                              double minComfort,
                              double minDifficulty) {

        double thresSec = clamp01(minSecurity - TOL);
        double thresConf = clamp01(minComfort - TOL);
        double thresDiff = clamp01(minDifficulty - TOL);

        double arcSec;
        double arcConf;
        double arcDiff;

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
                break;
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
