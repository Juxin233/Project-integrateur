package fr.insa.projetIntegrateur.RoutingService.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
import fr.insa.projetIntegrateur.RoutingService.service.PathService.PathResult;

class PathServiceTest {

    @Test
    void testCalculerAStar() throws Exception {
        PathService service = new PathService();
        long startId = 5561695614L;
        long endId = 13334782210L;

        // Standard algos return List<Noeud>
        List<Noeud> cheminDijkstra = service.calculerDijkstra(startId, endId, 2);
        List<Noeud> cheminA = service.calculerAstar(startId, endId, 2);

        // Constrained algos return PathResult
        PathResult resConstrainedDijkstra = service.calculerCheminFiltre(startId, endId, 2, 0.55, 0.5, 0.5);
        PathResult resConstrainedA = service.calculerCheminFiltreAstar(startId, endId, 2, 0.55, 0.5, 0.5);

        List<Noeud> cheminConstrainedDijkstra = resConstrainedDijkstra.path;
        List<Noeud> cheminConstrainedA = resConstrainedA.path;

        System.out.print("Dijkstra: " + cheminDijkstra + "\n");
        System.out.print("A*: " + cheminA + "\n");
        System.out.print("Constrained Dijkstra Path: " + cheminConstrainedDijkstra + "\n");
        System.out.print("Constrained A* Path: " + cheminConstrainedA + "\n");
        System.out.print("Relaxed (Dijkstra): " + resConstrainedDijkstra.constraintsRelaxed + "\n");
        System.out.print("Relaxed (A*): " + resConstrainedA.constraintsRelaxed + "\n");

        // Assertions for Standard Algos
        assertFalse(cheminDijkstra.isEmpty(), "Le chemin Dijkstra ne doit pas être vide");
        assertEquals(startId, cheminDijkstra.get(0).getId());
        assertEquals(endId, cheminDijkstra.get(cheminDijkstra.size() - 1).getId());

        assertEquals(startId, cheminA.get(0).getId());
        assertEquals(endId, cheminA.get(cheminA.size() - 1).getId());
        assertEquals(cheminDijkstra, cheminA, "Dijkstra and A* should find same path unconstrained");

        // Assertions for Constrained Algos
        assertFalse(cheminConstrainedDijkstra.isEmpty(), "Le chemin Constrained Dijkstra ne doit pas être vide");
        assertEquals(startId, cheminConstrainedDijkstra.get(0).getId());
        assertEquals(endId, cheminConstrainedDijkstra.get(cheminConstrainedDijkstra.size() - 1).getId());

        assertEquals(startId, cheminConstrainedA.get(0).getId());
        assertEquals(endId, cheminConstrainedA.get(cheminConstrainedA.size() - 1).getId());
        
        // In this test, assuming constraints are loose enough to find a path without relaxation,
        // or effectively verifying that they at least return consistent results.
        // We verify that the path service returns the correct structure.
        assertNotNull(resConstrainedDijkstra);
        assertNotNull(resConstrainedA);
        
        // Check consistency between constrained algos
        assertEquals(cheminConstrainedDijkstra, cheminConstrainedA, "Constrained A* and Dijkstra should match");
    }
}