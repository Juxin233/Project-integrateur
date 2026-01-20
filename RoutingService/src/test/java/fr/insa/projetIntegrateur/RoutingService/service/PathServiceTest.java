package fr.insa.projetIntegrateur.RoutingService.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
import fr.insa.projetIntegrateur.RoutingService.model.Reponse;

class PathServiceTest {

    @Test
    void testCalculerAStar() throws Exception {
        PathService service = new PathService();
        long startId = 5561695614L;
        long endId = 13334782210L;

        Reponse cheminDijkstra = service.calculerDijkstra(startId, endId,2,4);
        Reponse cheminA= service.calculerAstar(startId, endId,2,4);
        Reponse cheminConstrainedDijkstra=service.calculerCheminFiltre(startId, endId, 2,4, 0.9, 0.9, 0.9);
        Reponse cheminConstrainedA=service.calculerCheminFiltreAstar(startId, endId,   2,4, 0.9, 0.9, 0.9);
        //0.801229431274361 0.801229431274361 0.8479567415605785  (0.0, 0.0, 0.0)
        //0.801229431274361 0.801229431274361 0.8479567415605785  (0.5, 0.5, 0.5)
        //0.7506563965814037 0.7506563965814037 0.8109046316257341(0.6, 0.6, 0.6)-(1.0,1.0,1.0)
        System.out.print("Dijkstra: \n" + cheminDijkstra.getList() + "\n");
        System.out.println(cheminDijkstra.getConfort()+" "+cheminDijkstra.getDiff()+" "+cheminDijkstra.getRisque());
        System.out.print("\n");
        System.out.print("A*\n: " + cheminA.getList() + "\n");
        System.out.println(cheminA.getConfort()+" "+cheminA.getDiff()+" "+cheminA.getRisque());
        System.out.print("\n");
        System.out.print("Constrained Dijkstra Path: \n" + cheminConstrainedDijkstra.getList() + "\n");
        System.out.println(cheminConstrainedDijkstra.getConfort()+" "+cheminConstrainedDijkstra.getDiff()+" "+cheminConstrainedDijkstra.getRisque());
        System.out.print("\n");
        System.out.print("Constrained A* Path: \n" + cheminConstrainedA.getList() + "\n");
        System.out.println(cheminConstrainedA.getConfort()+" "+cheminConstrainedA.getDiff()+" "+cheminConstrainedA.getRisque());
        System.out.print("\n");
        System.out.print("Relaxed (Dijkstra): " + cheminConstrainedDijkstra.isProfil_change()+ "\n");
        System.out.print("Relaxed (A*): " + cheminConstrainedA.isProfil_change() + "\n");

        //comparaison entre Dijkstra et Astar
        assertFalse(cheminDijkstra.getList().isEmpty(), "Le chemin ne doit pas être vide");
        assertEquals(startId, cheminDijkstra.getList().get(0).getId());
        assertEquals(endId, cheminDijkstra.getList().get(cheminDijkstra.getList().size() - 1).getId());
        assertEquals(startId, cheminA.getList().get(0).getId());
        assertEquals(endId, cheminA.getList().get(cheminA.getList().size() - 1).getId());
        assertEquals(cheminDijkstra.getList(),cheminA.getList());
        
        //comparaison entre constrainedDijkstra et constrainedAstar
        assertFalse(cheminConstrainedDijkstra.getList().isEmpty(), "Le chemin ne doit pas être vide");
        assertEquals(startId, cheminConstrainedDijkstra.getList().get(0).getId());
        assertEquals(endId, cheminConstrainedDijkstra.getList().get(cheminConstrainedDijkstra.getList().size() - 1).getId());
        assertEquals(startId, cheminConstrainedA.getList().get(0).getId());
        assertEquals(endId, cheminConstrainedA.getList().get(cheminConstrainedA.getList().size() - 1).getId());
        assertEquals(cheminDijkstra.getList(),cheminConstrainedA.getList());

        // In this test, assuming constraints are loose enough to find a path without relaxation,
        // or effectively verifying that they at least return consistent results.
        // We verify that the path service returns the correct structure.
        assertNotNull(cheminConstrainedDijkstra.getList());
        assertNotNull(cheminConstrainedA.getList());

        // Check consistency between constrained algos
        assertEquals(cheminConstrainedDijkstra.getList(), cheminConstrainedA.getList(), "Constrained A* and Dijkstra should match");
    }

}
