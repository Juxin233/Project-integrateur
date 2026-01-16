package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import fr.insa.projetIntegrateur.RoutingService.service.PathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConstrainedDijkstra and ConstrainedAstar algorithms.
 * Includes verification of the Constraint Relaxation logic.
 */
public class ConstrainedRoutingTest {

    private Graph graph;
    private Noeud n1, n2, n3, n4;
    private long idCounter = 0; // To generate unique IDs for Arcs
    
    private double computePathLength(Graph g, List<Noeud> path) {
        if (path == null || path.isEmpty()) return 0.0;
        double length = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            Noeud from = path.get(i);
            Noeud to = path.get(i + 1);

            boolean found = false;
            for (Arc arc : g.getAdjacents(from.getId())) {
                if (arc.getDestination().getId() == to.getId()) {
                    length += arc.getLongueur();
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("No arc found between " + from.getId() + " and " + to.getId());
            }
        }
        return length;
    }

    @BeforeEach
    void setup() {
        graph = new Graph();

        // Create nodes
        n1 = new Noeud(1, 43.60, 1.44);  // Toulouse
        n2 = new Noeud(2, 43.60005, 1.44005);
        n3 = new Noeud(3, 43.60010, 1.44010);
        n4 = new Noeud(4, 43.60015, 1.44015);

        graph.ajouterNoeud(n1);
        graph.ajouterNoeud(n2);
        graph.ajouterNoeud(n3);
        graph.ajouterNoeud(n4);

        // Create arcs (bidirectional)
        // Params: From, To, Length, Security, Comfort, Difficulty
        
        // 1->2: Very Safe (0.9), Good Comfort
        addArc(n1, n2, 100, 0.9, 0.8, 0.9); 
        
        // 2->3: "High Risk" context -> Lower Security (0.6). 
        // This will be passable for req=0.5, but rejected for req=0.9
        addArc(n2, n3, 150, 0.6, 0.7, 0.8); 
        
        // 3->4: Good (0.9)
        addArc(n3, n4, 200, 0.9, 0.9, 0.9); 
        
        // 1->4: Direct but longer (600). Very Safe (0.95) so it survives high constraints.
        addArc(n1, n4, 800, 0.95, 0.7, 0.7); 

        // Reverse arcs
        addArc(n2, n1, 100, 0.9, 0.8, 0.9);
        addArc(n3, n2, 150, 0.6, 0.7, 0.8);
        addArc(n4, n3, 200, 0.9, 0.9, 0.9);
        addArc(n4, n1, 800, 0.95, 0.7, 0.7);
    }

    // Helper to adapt to the 11-argument Arc constructor
    private void addArc(Noeud from, Noeud to, double length, double security, double comfort, double diff) {
        Arc a = new Arc(
            from, 
            to, 
            length, 
            "test_road", 
            ++idCounter, 
            security,    // risquePieton
            0.0,         
            comfort,     // confortPieton
            0.0,         
            0.0,         
            diff         // diffPieton
        );
        graph.ajouterArc(a);
    }

    @Test
    void testDijkstra_BasicShortestPath() {
        ConstrainedDijkstra algo = new ConstrainedDijkstra();
        // Req 0.5: All arcs (0.6, 0.9, 0.95) are >= 0.45. All are valid.
        PathService.PathResult result = algo.shortestPath(graph, 1, 4, 2, 0.5, 0.5, 0.5);

        assertFalse(result.path.isEmpty(), "Dijkstra should find a valid path");
        assertFalse(result.constraintsRelaxed, "Constraints should NOT be relaxed for easy path");

        double totalLength = computePathLength(graph, result.path);
        // Should choose 1->2->3->4 (100+150+200 = 450) over 1->4 (800)
        assertEquals(450, totalLength, 1e-6, "Expected 1->2->3->4 path (100+150+200)");
    }

    @Test
    void testAstar_BasicShortestPath() {
        ConstrainedAstar algo = new ConstrainedAstar();
        PathService.PathResult result = algo.shortestPath(graph, 1, 4, 2, 0.5, 0.5, 0.5);

        assertFalse(result.path.isEmpty(), "A* should find a valid path");
        assertFalse(result.constraintsRelaxed);

        double totalLength = computePathLength(graph, result.path);
        assertEquals(450, totalLength, 1e-6);
    }

    @Test
    void testDijkstra_WithHighSecurityRequirement() {
        ConstrainedDijkstra algo = new ConstrainedDijkstra();
        // Req 0.9: Threshold is 0.85.
        // Arc 2->3 (Security 0.6) < 0.85 -> REJECTED.
        // Arc 1->4 (Security 0.95) >= 0.85 -> ACCEPTED.
        PathService.PathResult result = algo.shortestPath(graph, 1, 4, 2, 0.9, 0.5, 0.5);

        assertFalse(result.path.isEmpty());
        // Should NOT relax because 1->4 is a valid alternative path that meets criteria
        assertFalse(result.constraintsRelaxed); 

        double totalLength = computePathLength(graph, result.path);
        // Must take the long direct route
        assertEquals(800, totalLength, 1e-6, "High security should force longer but safer path");
    }

    @Test
    void testDijkstra_Relaxation_Logic() {
        // SCENARIO: 
        // We add a new node 5 connected ONLY to node 4 via a 'Bad' road (Security 0.2).
        // We request a path from 1 -> 5 with Security 0.8.
        // 1->4 is safe (0.95). Arriving at 4, the only way out is 4->5 (0.2).
        // Req 0.8 requires > 0.75. 0.2 is fails.
        // Algorithm MUST relax constraints at Node 4 until 4->5 becomes valid.
        
        Noeud n5 = new Noeud(5, 43.60020, 1.44020);
        graph.ajouterNoeud(n5);
        addArc(n4, n5, 50, 0.2, 0.5, 0.5); // Very unsafe link

        ConstrainedDijkstra algo = new ConstrainedDijkstra();
        PathService.PathResult result = algo.shortestPath(graph, 1, 5, 2, 0.8, 0.5, 0.5);

        assertFalse(result.path.isEmpty(), "Path should be found via relaxation");
        assertTrue(result.constraintsRelaxed, "Constraints MUST have been relaxed to cross the unsafe arc");
        
        assertEquals(5, result.path.get(result.path.size()-1).getId());
    }

    @Test
    void testAstar_Relaxation_Logic() {
        // Same scenario as above for A*
        Noeud n5 = new Noeud(5, 43.60020, 1.44020);
        graph.ajouterNoeud(n5);
        addArc(n4, n5, 50, 0.2, 0.5, 0.5);

        ConstrainedAstar algo = new ConstrainedAstar();
        PathService.PathResult result = algo.shortestPath(graph, 1, 5, 2, 0.8, 0.5, 0.5);

        assertFalse(result.path.isEmpty(), "A* should find path via relaxation");
        assertTrue(result.constraintsRelaxed, "Constraints MUST have been relaxed");
        
        assertEquals(5, result.path.get(result.path.size()-1).getId());
    }

    @Test
    void testAstar_And_Dijkstra_GiveSameResults() {
        ConstrainedAstar astar = new ConstrainedAstar();
        ConstrainedDijkstra dijkstra = new ConstrainedDijkstra();

        // Use moderate constraints where no relaxation is needed
        PathService.PathResult resA = astar.shortestPath(graph, 1, 4, 2, 0.6, 0.6, 0.6);
        PathService.PathResult resD = dijkstra.shortestPath(graph, 1, 4, 2, 0.6, 0.6, 0.6);

        double lenA = computePathLength(graph, resA.path);
        double lenD = computePathLength(graph, resD.path);
        assertEquals(lenD, lenA, 1e-6, "A* and Dijkstra should return same path cost");
    }
}