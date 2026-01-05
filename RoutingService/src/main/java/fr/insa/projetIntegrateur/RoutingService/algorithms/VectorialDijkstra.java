package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import java.util.*;

public class VectorialDijkstra {

    // Helper class to track state in the Priority Queue
    private static class Label implements Comparable<Label> {
        long nodeId;
        VectorCost cost;
        Label previous; // To reconstruct the path
        Arc arcUsed;    // The arc used to reach this node

        Label(long nodeId, VectorCost cost, Label previous, Arc arcUsed) {
            this.nodeId = nodeId;
            this.cost = cost;
            this.previous = previous;
            this.arcUsed = arcUsed;
        }

        // For PQ: We need a way to sort. Since there is no single "best",
        // we usually sort by a primary component (e.g., c1/Distance) or a weighted sum
        // just to drive the search forward. This doesn't affect correctness, only speed.
        @Override
        public int compareTo(Label o) {
            return Double.compare(this.cost.c1, o.cost.c1);
        }
    }

    /**
     * Calculates the Pareto Frontier of paths between start and end.
     * Returns a LIST of paths (each path is a List<Arc>).
     */
    public List<List<Arc>> findParetoPaths(Graph g, long startId, long endId) {
        
        // Store ALL non-dominated labels reaching a node
        Map<Long, List<Label>> nodeLabels = new HashMap<>();
        
        // Priority Queue for the search frontier
        PriorityQueue<Label> pq = new PriorityQueue<>();

        // Init
        Label startLabel = new Label(startId, VectorCost.zero(), null, null);
        pq.add(startLabel);
        addLabelToNode(nodeLabels, startId, startLabel);

        List<Label> finalLabels = new ArrayList<>();

        while (!pq.isEmpty()) {
            Label current = pq.poll();

            // If we reached destination, store this as a potential solution
            // But continue searching, because other paths might optimize other vector components
            if (current.nodeId == endId) {
                finalLabels.add(current);
                continue; 
            }

            // Expansion
            for (Arc arc : g.getAdjacents(current.nodeId)) {
                long neighborId = arc.getDestination().getId();

                // 1. Calculate new Vector Cost
                VectorCost arcCost = extractCostFromArc(arc);
                VectorCost newTotalCost = current.cost.add(arcCost);

                // 2. Pareto Check: Is this new path dominated by any existing path to the neighbor?
                if (!isDominated(nodeLabels, neighborId, newTotalCost)) {
                    
                    // 3. Create new Label
                    Label newLabel = new Label(neighborId, newTotalCost, current, arc);
                    
                    // 4. Filter existing labels at neighbor: Remove those dominated by the NEW one
                    // (This keeps the search efficient)
                    removeDominatedLabels(nodeLabels, neighborId, newTotalCost);

                    // 5. Add to storage and PQ
                    addLabelToNode(nodeLabels, neighborId, newLabel);
                    pq.add(newLabel);
                }
            }
        }

        // Reconstruct all paths from the final accepted labels at the destination
        return reconstructAllPaths(finalLabels);
    }

    // --- Helper Methods ---

    // Define how to map Arc attributes to your 4 Vector Components
    private VectorCost extractCostFromArc(Arc arc) {
        // Example Mapping based on your Arc.java:
        // C1: Length
        // C2: Time (Walking)
        // C3: Risk (Pedestrian)
        // C4: Discomfort (inverse of Comfort, assuming comfort is 0-10, or just generic cost)
        
        double c1 = arc.getLongueur();
        double c2 = arc.getTempsMarche(); 
        double c3 = arc.getRisquePieton(); 
        double c4 = (10.0 - arc.getConfortPieton()); // Example: Minimize discomfort

        return new VectorCost(c1, c2, c3, c4);
    }

    private boolean isDominated(Map<Long, List<Label>> nodeLabels, long nodeId, VectorCost newCost) {
        List<Label> existing = nodeLabels.getOrDefault(nodeId, Collections.emptyList());
        for (Label l : existing) {
            if (l.cost.dominates(newCost) || l.cost.equals(newCost)) {
                return true; // Use strict dominance or equality to prune
            }
        }
        return false;
    }

    private void removeDominatedLabels(Map<Long, List<Label>> nodeLabels, long nodeId, VectorCost newCost) {
        if (!nodeLabels.containsKey(nodeId)) return;
        List<Label> labels = nodeLabels.get(nodeId);
        labels.removeIf(l -> newCost.dominates(l.cost));
    }

    private void addLabelToNode(Map<Long, List<Label>> nodeLabels, long nodeId, Label label) {
        nodeLabels.computeIfAbsent(nodeId, k -> new ArrayList<>()).add(label);
    }

    private List<List<Arc>> reconstructAllPaths(List<Label> labels) {
        List<List<Arc>> allPaths = new ArrayList<>();
        
        // Filter final labels one last time (compare them against each other)
        List<Label> paretoOptimalLabels = new ArrayList<>();
        for (Label candidate : labels) {
            boolean dominated = false;
            for (Label other : labels) {
                if (candidate != other && other.cost.dominates(candidate.cost)) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated) paretoOptimalLabels.add(candidate);
        }

        // Backtrack
        for (Label l : paretoOptimalLabels) {
            LinkedList<Arc> path = new LinkedList<>();
            Label curr = l;
            while (curr.previous != null) {
                path.addFirst(curr.arcUsed);
                curr = curr.previous;
            }
            allPaths.add(path);
        }
        return allPaths;
    }
}