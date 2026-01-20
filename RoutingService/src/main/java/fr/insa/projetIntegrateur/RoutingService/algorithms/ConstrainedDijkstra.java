package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import java.util.*;

public class ConstrainedDijkstra {
    private static final double TOL = 0.05;

    // "Global" variables for the algorithm instance
    private boolean constraintsRelaxed = false;
    private double curSec;
    private double curConf;
    private double curDiff;

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

    public Reponse shortestPath(Graph g,
                                    long startId,
                                    long endId,
                                    int type,
                                    int access,
                                    double minSecurity,
                                    double minComfort,
                                    double minDifficulty) {

        // Initialize global tracking variables
        this.constraintsRelaxed = false;
        this.curSec = minSecurity;
        this.curConf = minComfort;
        this.curDiff = minDifficulty;
        if (g == null) return new Reponse(Collections.emptyList(),0,0,0,type,false);
        Noeud start = g.getNoeud(startId);
        Noeud goal = g.getNoeud(endId);
        if (start == null || goal == null) return new Reponse(Collections.emptyList(),0,0,0,type,false);
        if (startId == endId) return new Reponse(Collections.emptyList(),0,0,0,type,false);

        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Arc> prevArc = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        PriorityQueue<NodeDist> pq = new PriorityQueue<>();

        // init
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

            // --- RELAXATION LOGIC START ---
            // Check if ANY arc is valid with current constraints to an UNVISITED node.
            // If not, relax constraints and loop until at least one is valid or we hit 0.
            while (true) {
                boolean hasValidArc = false;
                for (Arc arc : adj) {
                    if (arc == null) continue;
                    int roadType = arc.getType_route();
                    if (roadType != type && roadType != 2) continue;

                    // CRITICAL FIX: Ignore arcs pointing to already visited nodes
                    // Otherwise, we never relax because the back-edge is usually valid.
                    if (arc.getDestination() != null && visited.contains(arc.getDestination().getId())) {
                        continue;
                    }

                    // Check validity using the "Global" current variables
                    if (isArcValid(arc, type,access ,this.curSec, this.curConf, this.curDiff)) {
                        hasValidArc = true;
                        break;
                    }
                }

                if (hasValidArc) {
                    break; // Found at least one valid way out, proceed
                }

                // If we are here, NO arc is valid to unvisited nodes. Check if we can relax.
                // Stop if all are already 0 (cannot relax further)
                if (this.curSec <= 0 && this.curConf <= 0 && this.curDiff <= 0) {
                    break;
                }

                // Edit the "Global" vector: reduce non-zero components by 0.05
                if (this.curSec > 0) this.curSec = Math.max(0.0, this.curSec - 0.05);
                if (this.curConf > 0) this.curConf = Math.max(0.0, this.curConf - 0.05);
                if (this.curDiff > 0) this.curDiff = Math.max(0.0, this.curDiff - 0.05);

                // Set global boolean to true
                this.constraintsRelaxed = true;

                // Loop continues to check validity with new 'criteria'
            }
            // --- RELAXATION LOGIC END ---

            double du = dist.getOrDefault(u, Double.POSITIVE_INFINITY);

            for (Arc arc : adj) {
                if (arc == null) continue;

                int roadType = arc.getType_route();
                if (roadType != type && roadType != 2) continue;

                // Use the updated Global constraints
                if (!isArcValid(arc, type, access,this.curSec, this.curConf, this.curDiff)) continue;

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
        if (!prevArc.containsKey(endId)) return new Reponse(Collections.emptyList(),0,0,0,type,false);

        return reconstructPath(g, prevArc, startId, endId,type,this.constraintsRelaxed);
    }

    private boolean isArcValid(Arc arc,
                              int type,
                              int access,
                              double minSecurity,
                              double minComfort,
                              double minDifficulty) {

        double thresSec = clamp01(minSecurity - TOL);
        double thresConf = clamp01(minComfort - TOL);
        double thresDiff = clamp01(minDifficulty - TOL);

        double arcSec;
        double arcConf;
        double arcDiff;
        
        if(type != arc.getType_route() && arc .getType_route() != 2) {
        	return false;
        }
        
        if(type == 0 && arc.getType_Pieton() != access && arc.getType_Pieton() != 4) {
        	return false;
        }
        
        if(type == 1 && arc.getType_Velo() != 1) {
        	return false;
        }
        
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

    private Reponse reconstructPath(Graph graphe, Map<Long, Arc> cameFrom, long startId, long goalId,int type,boolean constraintsRelaxed) {
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

        return new Reponse(path,confort,diff,risque,type,constraintsRelaxed);
    }
}
