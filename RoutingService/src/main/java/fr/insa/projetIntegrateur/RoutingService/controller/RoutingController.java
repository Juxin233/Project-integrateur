package fr.insa.projetIntegrateur.RoutingService.controller;

import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
import fr.insa.projetIntegrateur.RoutingService.service.PathService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @GetMapping("/astar")
    public List<Noeud> astar(@RequestParam long start, @RequestParam long end) {
        return service.calculerAstar(start, end, 2);
    }

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