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

        List<Noeud> chemin = service.calculerDijkstra(startId, endId);
        List<Noeud> cheminA= service.calculerAstar(startId, endId);
        System.out.print(chemin);
        System.out.print("\n");
        System.out.print(cheminA);
        assertFalse(chemin.isEmpty(), "Le chemin ne doit pas être vide");
        assertEquals(startId, chemin.get(0).getId());
        assertEquals(endId, chemin.get(chemin.size() - 1).getId());
        assertEquals(startId, cheminA.get(0).getId());
        assertEquals(endId, cheminA.get(chemin.size() - 1).getId());
        assertEquals(chemin,cheminA);
    }

}
