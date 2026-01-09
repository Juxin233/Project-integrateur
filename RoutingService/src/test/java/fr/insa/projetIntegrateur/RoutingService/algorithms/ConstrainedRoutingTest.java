package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConstrainedDijkstra and ConstrainedAstar algorithms.
 * * ADAPTATION NOTE:
 * The test values for 'Risk' have been converted to 'Security' (Higher is Better)
 * to align with the filter logic: (ArcValue >= UserReq - 0.05).
 * - "High Risk" is now represented as Lower Security value.
 * - "Low Risk" is now represented as Higher Security value.
 */
public class ConstrainedRoutingTest {

    private Graph graph;
    private Noeud n1, n2, n3, n4;
    private long idCounter = 0; // To generate unique IDs for Arcs
    
    private double computePathLength(Graph g, List<Noeud> path) {
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

        graph.ajouterNoeud(n1); // Fixed: addNoeud -> ajouterNoeud
        graph.ajouterNoeud(n2);
        graph.ajouterNoeud(n3);
        graph.ajouterNoeud(n4);

        // Create arcs (bidirectional)
        // Params: From, To, Length, Security, Comfort, Difficulty
        // NOTE: We map 'Security' to the 'risquePieton' field for this test.
        
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
        // We assume 'risquePieton' holds the SECURITY score (0-1, Higher=Better)
        Arc a = new Arc(
            from, 
            to, 
            length, 
            "test_road", 
            ++idCounter, // Generate ID
            security,    // risquePieton
            0.0,         // risqueVelo (unused)
            comfort,     // confortPieton
            0.0,         // confortVelo (unused)
            0.0,         // diffVelo (unused)
            diff         // diffPieton
        );
        graph.ajouterArc(a); // Fixed: addArc -> ajouterArc
    }

    @Test
    void testDijkstra_BasicShortestPath() {
        ConstrainedDijkstra algo = new ConstrainedDijkstra();
        // Req 0.5: All arcs (0.6, 0.9, 0.95) are >= 0.45. All are valid.
        List<Noeud> path = algo.shortestPath(graph, 1, 4,2, 0.5, 0.5, 0.5);

        assertFalse(path.isEmpty(), "Dijkstra should find a valid path");
        double totalLength = computePathLength(graph,path);
        // Should choose 1->2->3->4 (100+150+200 = 450) over 1->4 (800)
        assertEquals(450, totalLength, 1e-6, "Expected 1->2->3->4 path (100+150+200)");
    }

    @Test
    void testAstar_BasicShortestPath() {
        ConstrainedAstar algo = new ConstrainedAstar();
        List<Noeud> path = algo.shortestPath(graph, 1, 4,2, 0.5, 0.5, 0.5);

        assertFalse(path.isEmpty(), "A* should find a valid path");
        double totalLength = computePathLength(graph,path);
        assertEquals(450, totalLength, 1e-6, "Expected 1->2->3->4 path (100+150+200)");
    }

    @Test
    void testDijkstra_WithHighSecurityRequirement() {
        ConstrainedDijkstra algo = new ConstrainedDijkstra();
        // Req 0.9: Threshold is 0.85.
        // Arc 2->3 (Security 0.6) < 0.85 -> REJECTED.
        // Arc 1->4 (Security 0.95) >= 0.85 -> ACCEPTED.
        List<Noeud> path = algo.shortestPath(graph, 1, 4, 2,0.9, 0.5, 0.5);

        assertFalse(path.isEmpty());
        double totalLength = computePathLength(graph,path);
        // Must take the long direct route
        assertEquals(800, totalLength, 1e-6, "High security should force longer but safer path");
    }

    @Test
    void testAstar_WithHighSecurityRequirement() {
        ConstrainedAstar algo = new ConstrainedAstar();
        List<Noeud> path = algo.shortestPath(graph, 1, 4,2, 0.9, 0.5, 0.5);

        assertFalse(path.isEmpty());
        double totalLength = computePathLength(graph,path);
        assertEquals(800, totalLength, 1e-6, "High security should force longer but safer path");
    }

    @Test
    void testAstar_And_Dijkstra_GiveSameResults() {
        ConstrainedAstar astar = new ConstrainedAstar();
        ConstrainedDijkstra dijkstra = new ConstrainedDijkstra();

        List<Noeud> pathA = astar.shortestPath(graph, 1, 4,2, 0.6, 0.6, 0.6);
        List<Noeud> pathD = dijkstra.shortestPath(graph, 1, 4,2, 0.6, 0.6, 0.6);

        double lenA = computePathLength(graph,pathA);
        double lenD = computePathLength(graph,pathD);
        assertEquals(lenD, lenA, 1e-6, "A* and Dijkstra should return same path cost");
    }
}
