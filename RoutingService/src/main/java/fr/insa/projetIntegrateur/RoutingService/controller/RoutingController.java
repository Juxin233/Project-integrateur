package fr.insa.projetIntegrateur.RoutingService.controller;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
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
    public List<Noeud> route(
            @RequestParam long start,
            @RequestParam long end) {

        return service.calculerDijkstra(start, end,2);
    }
    
    // Existing simple Dijkstra
    @GetMapping("/astar")
    public List<Noeud> astar(
            @RequestParam long start,
            @RequestParam long end) {

        return service.calculerAstar(start, end,2);
    }
    
    // NEW: Constrained Dijkstra
    // Example call: /api/route/constrained?start=1&end=10&sec=0.5&conf=0.8&diff=0.2
    @GetMapping("/constrained")
    public List<Noeud> routeConstrained(
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "0") double sec,
            @RequestParam(defaultValue = "0") double conf,
            @RequestParam(defaultValue = "0") double diff) {

        List<Noeud> path = service.calculerCheminFiltre(start, end,2, sec, conf, diff);

        return path;
    }

    // NEW: Constrained A*
    @GetMapping("/constrained/astar")
    public List<Noeud> routeConstrainedAstar(
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "0") double sec,
            @RequestParam(defaultValue = "0") double conf,
            @RequestParam(defaultValue = "0") double diff) {

        List<Noeud> path = service.calculerCheminFiltreAstar(start, end, 2,sec, conf, diff);

        if (path.isEmpty()) {
            // Optional: Handle no path found (e.g., return 404 or empty list)
        }

        return path;
    }
}
