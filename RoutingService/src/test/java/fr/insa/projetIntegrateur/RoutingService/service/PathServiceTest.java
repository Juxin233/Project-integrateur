package fr.insa.projetIntegrateur.RoutingService.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;

class PathServiceTest {

	@Test
	 void testCalculerAStar() throws Exception {
        PathService service = new PathService();
        long startId = 5561695614L;
        long endId = 13334782210L;

        List<Long> chemin = service.calculerDijkstra(startId, endId);
        System.out.print(chemin);
        assertFalse(chemin.isEmpty(), "Le chemin ne doit pas être vide");
        assertEquals(startId, chemin.get(0));
        assertEquals(endId, chemin.get(chemin.size() - 1));
    }

}
