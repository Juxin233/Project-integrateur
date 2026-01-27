package fr.insa.projetIntegrateur.RoutingService.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
import fr.insa.projetIntegrateur.RoutingService.service.PathService;

@RestController
@RequestMapping("/api/route")
public class RoutingController {

    private final PathService service;

    public RoutingController(PathService service) {
        this.service = service;
    }

    @GetMapping("/dijkstra")
    public List<Noeud> route(@RequestParam long start, @RequestParam long end) {
        return service.calculerDijkstra(start, end, 2);
    }
    
    // Existing simple Dijkstra
    @GetMapping("/astar")
    public List<Noeud> astar(@RequestParam long start, @RequestParam long end) {
        return service.calculerAstar(start, end, 2);
    }
    
    // NEW: Constrained Dijkstra
    // Example call: /api/route/constrained?start=1&end=10&sec=0.5&conf=0.8&diff=0.2
    @GetMapping("/constrained")
    public PathService.PathResult routeConstrained(
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "0") double sec,
            @RequestParam(defaultValue = "0") double conf,
            @RequestParam(defaultValue = "0") double diff) {
        return service.calculerCheminFiltre(start, end, 2, sec, conf, diff);
    }

    @GetMapping("/constrained/astar")
    public PathService.PathResult routeConstrainedAstar(
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "0") double sec,
            @RequestParam(defaultValue = "0") double conf,
            @RequestParam(defaultValue = "0") double diff) {
        return service.calculerCheminFiltreAstar(start, end, 2, sec, conf, diff);
    }

    // --- NEW ENDPOINT ---
    @GetMapping("/nearest")
    public long getNearestNode(@RequestParam double lat, @RequestParam double lon) {
        return service.findNearestNode(lat, lon);
    }
}