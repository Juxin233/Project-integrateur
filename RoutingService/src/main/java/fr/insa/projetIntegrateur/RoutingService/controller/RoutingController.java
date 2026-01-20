package fr.insa.projetIntegrateur.RoutingService.controller;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
import fr.insa.projetIntegrateur.RoutingService.model.Reponse;
import fr.insa.projetIntegrateur.RoutingService.service.PathService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/route")
public class RoutingController {

    private final PathService service;

    public RoutingController(PathService service) {
        this.service = service;
    }

    // Existing simple Dijkstra
    @GetMapping("/dijkstra")
    public Reponse route(
            @RequestParam long start,
            @RequestParam long end) {

        return service.calculerDijkstra(start, end,2,2);
    }

    // Existing simple A*
    @GetMapping("/astar")
    public Reponse astar(
            @RequestParam long start,
            @RequestParam long end) {

        return service.calculerAstar(start, end,2,2);
    }
    // NEW: Constrained Dijkstra
    // Example call: /api/route/constrained?start=1&end=10&sec=0.5&conf=0.8&diff=0.2
    @GetMapping("/constrained")
    public Reponse routeConstrained(
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "0.0") double sec,
            @RequestParam(defaultValue = "0.0") double conf,
            @RequestParam(defaultValue = "0.0") double diff) {

        Reponse path = service.calculerCheminFiltre(start, end,2, 2,sec, conf, diff);

        return path;
    }

    // NEW: Constrained A*
    // Returns PathResult (path + relaxed boolean)
    @GetMapping("/constrained/astar")
    public Reponse routeConstrainedAstar(
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "0") double sec,
            @RequestParam(defaultValue = "0") double conf,
            @RequestParam(defaultValue = "0") double diff) {

        Reponse path = service.calculerCheminFiltreAstar(start, end, 2,2,sec, conf, diff);

        if (path.getList().isEmpty()) {
            // Optional: Handle no path found (e.g., return 404 or empty list)
        }

        return path;
    }

    // --- NEW ENDPOINT ---
    @GetMapping("/nearest")
    public long getNearestNode(@RequestParam double lat, @RequestParam double lon) {
        return service.findNearestNode(lat, lon);
    }
}