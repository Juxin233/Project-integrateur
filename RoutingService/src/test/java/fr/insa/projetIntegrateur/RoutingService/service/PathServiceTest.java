package fr.insa.projetIntegrateur.RoutingService.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;

class PathServiceTest {

	@Test
	 void testCalculerAStar() throws Exception {
        PathService service = new PathService();
        long startId = 5561695614L;
        long endId = 13334782210L;

        List<Noeud> cheminDijkstra = service.calculerDijkstra(startId, endId,2);
        List<Noeud> cheminA= service.calculerAstar(startId, endId,2);
        List<Noeud> cheminConstrainedDijkstra=service.calculerCheminFiltre(startId, endId, 2, 0.55, 0.5, 0.5);
        List<Noeud> cheminConstrainedA=service.calculerCheminFiltreAstar(startId, endId, 2, 0.55, 0.5, 0.5);
        System.out.print(cheminDijkstra);
        System.out.print("\n");
        System.out.print(cheminA);
        System.out.print("\n");
        System.out.print(cheminConstrainedDijkstra);
        System.out.print("\n");
        System.out.print(cheminConstrainedA);
        assertFalse(cheminDijkstra.isEmpty(), "Le chemin ne doit pas être vide");
        assertEquals(startId, cheminDijkstra.get(0).getId());
        assertEquals(endId, cheminDijkstra.get(cheminDijkstra.size() - 1).getId());
        assertEquals(startId, cheminA.get(0).getId());
        assertEquals(endId, cheminA.get(cheminA.size() - 1).getId());
        assertEquals(cheminDijkstra,cheminA);
        assertFalse(cheminConstrainedDijkstra.isEmpty(), "Le chemin ne doit pas être vide");
        assertEquals(startId, cheminConstrainedDijkstra.get(0).getId());
        assertEquals(endId, cheminConstrainedDijkstra.get(cheminConstrainedDijkstra.size() - 1).getId());
        assertEquals(startId, cheminConstrainedA.get(0).getId());
        assertEquals(endId, cheminConstrainedA.get(cheminConstrainedA.size() - 1).getId());
        assertEquals(cheminDijkstra,cheminConstrainedA);
    }

}
