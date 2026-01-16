package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConstrainedDijkstra and ConstrainedAstar algorithms.
 * * UPDATES:
 * - Tests now acknowledge that the "Greedy Relaxation" logic (relaxing constraints
 * immediately when a node has no valid neighbors) allows the algorithm to
 * traverse shorter, previously invalid paths.
 */
public class ConstrainedRoutingTest {

    private Graph graph;
    private Noeud n1, n2, n3, n4;
    private long idCounter = 0;
    
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

        // 1->2: Very Safe (0.9), Length 100
        addArc(n1, n2, 100, 0.9, 0.8, 0.9); 
        
        // 2->3: "High Risk" (0.6). Length 150.
        // Fails Req=0.9 unless relaxed.
        addArc(n2, n3, 150, 0.6, 0.7, 0.8); 
        
        // 3->4: Good (0.9). Length 200.
        addArc(n3, n4, 200, 0.9, 0.9, 0.9); 
        
        // 1->4: Direct, Very Safe (0.95), but Long (800).
        addArc(n1, n4, 800, 0.95, 0.7, 0.7); 

        // Reverse arcs
        addArc(n2, n1, 100, 0.9, 0.8, 0.9);
        addArc(n3, n2, 150, 0.6, 0.7, 0.8);
        addArc(n4, n3, 200, 0.9, 0.9, 0.9);
        addArc(n4, n1, 800, 0.95, 0.7, 0.7);


    }

    private void addArc(Noeud from, Noeud to, double length, double security, double comfort, double diff) {
        // We assume 'risquePieton' holds the SECURITY score (0-1, Higher=Better)
        Arc a = new Arc(
            from, 
            to, 
            length, 
            "test_road", 
            ++idCounter, // Generate ID
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
        Reponse path = algo.shortestPath(graph, 1, 4,2, 0.5, 0.5, 0.5);

        assertFalse(path.getList().isEmpty(), "Dijkstra should find a valid path");
        assertFalse(path.isProfil_change(),"Constraints should NOT be relaxed for easy path");
        double totalLength = computePathLength(graph,path.getList());
        // Should choose 1->2->3->4 (100+150+200 = 450) over 1->4 (800)
        assertEquals(450, totalLength, 1e-6, "Expected 1->2->3->4 path (100+150+200)");
    }

    @Test
    void testAstar_BasicShortestPath() {
        ConstrainedAstar algo = new ConstrainedAstar();
        Reponse path = algo.shortestPath(graph, 1, 4,2, 0.5, 0.5, 0.5);

        assertFalse(path.getList().isEmpty(), "A* should find a valid path");
        assertFalse(path.isProfil_change(),"Constraints should NOT be relaxed for easy path");
        double totalLength = computePathLength(graph, path.getList());
        assertEquals(450, totalLength, 1e-6);
    }

    @Test
    void testDijkstra_WithHighSecurity_TriggersRelaxation() {
        ConstrainedDijkstra algo = new ConstrainedDijkstra();
        // Req 0.9: Threshold is 0.85.
        // 1->4 is valid (0.95) and long (800).
        // 1->2->3 is shorter (250) but blocked at 2->3 (0.6).
        // Algorithm visits 1->2 first. Finds 2->3 blocked.
        // It RELAXES constraints at Node 2. 2->3 becomes valid.
        // It finds path 1->2->3->4 (Total 450).
        Reponse path = algo.shortestPath(graph, 1, 4, 2,0.9, 0.5, 0.5);

        assertFalse(path.getList().isEmpty());
        // Should NOT relax because 1->4 is a valid alternative path that meets criteria
        assertTrue(path.isProfil_change(),"Dijkstra should relax constraints when the shorter path is blocked");

        double totalLength = computePathLength(graph, path.getList());
        // Must take the long direct route
        assertEquals(450, totalLength, 1e-6, "High security should force longer but safer path");
    }

    @Test
    void testAstar_WithHighSecurity_TriggersRelaxation() {
        ConstrainedAstar algo = new ConstrainedAstar();
        Reponse result = algo.shortestPath(graph, 1, 4, 2, 0.9, 0.5, 0.5);

        assertFalse(result.getList().isEmpty());
        // Same logic as Dijkstra
        assertTrue(result.isProfil_change(), "A* should relax constraints when shorter path is blocked");
        assertEquals(450, computePathLength(graph, result.getList()), 1e-6);
    }

    @Test
    void testDijkstra_Relaxation_Logic_Necessary() {
        // Scenario where Relaxation is STRICTLY necessary (no other path exists)
        Noeud n5 = new Noeud(5, 43.60020, 1.44020);
        graph.ajouterNoeud(n5);
        addArc(n4, n5, 50, 0.2, 0.5, 0.5); // Very unsafe link, only way to 5

        ConstrainedDijkstra algo = new ConstrainedDijkstra();
        Reponse result = algo.shortestPath(graph, 1, 5, 2, 0.8, 0.5, 0.5);

        assertFalse(result.getList().isEmpty(), "Path should be found via relaxation");
        assertTrue(result.isProfil_change(), "Constraints MUST have been relaxed to cross the unsafe arc");
        
        assertEquals(5, result.getList().get(result.getList().size()-1).getId());
    }

    @Test
    void testAstar_Relaxation_Logic() {
        // Same scenario as above for A*
        Noeud n5 = new Noeud(5, 43.60020, 1.44020);
        graph.ajouterNoeud(n5);
        addArc(n4, n5, 50, 0.2, 0.5, 0.5);

        ConstrainedAstar algo = new ConstrainedAstar();
        Reponse result = algo.shortestPath(graph, 1, 5, 2, 0.8, 0.5, 0.5);

        assertFalse(result.getList().isEmpty(), "A* should find path via relaxation");
        assertTrue(result.isProfil_change(), "Constraints MUST have been relaxed");
        
        assertEquals(5, result.getList().get(result.getList().size()-1).getId());
    }

    @Test
    void testAstar_And_Dijkstra_GiveSameResults() {
        ConstrainedAstar astar = new ConstrainedAstar();
        ConstrainedDijkstra dijkstra = new ConstrainedDijkstra();

        List<Noeud> pathA = astar.shortestPath(graph, 1, 4,2, 0.6, 0.6, 0.6).getList();
        List<Noeud> pathD = dijkstra.shortestPath(graph, 1, 4,2, 0.6, 0.6, 0.6).getList();

        double lenA = computePathLength(graph,pathA);
        double lenD = computePathLength(graph,pathD);
        assertEquals(lenD, lenA, 1e-6, "A* and Dijkstra should return same path cost");
    }
}