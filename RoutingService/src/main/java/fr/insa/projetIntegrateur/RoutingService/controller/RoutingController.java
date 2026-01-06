package fr.insa.projetIntegrateur.RoutingService.controller;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.service.PathService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/route")
public class RoutingController {

    private final PathService service;

    public RoutingController(PathService service) {
        this.service = service;
    }

    // Existing simple Dijkstra
    @GetMapping("/dijkstra")
    public List<Map<String, Object>> route(@RequestParam long start, @RequestParam long end) {
        return service.calculerDijkstra(start, end).stream().map(Arc::toMap).toList();
    }

    // NEW: Constrained Dijkstra
    // Example call: /api/route/constrained?start=1&end=10&sec=0.5&conf=0.8&diff=0.2
    @GetMapping("/constrained")
    public List<Map<String, Object>> routeConstrained(
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "0") double sec,
            @RequestParam(defaultValue = "0") double conf,
            @RequestParam(defaultValue = "0") double diff) {

        List<Arc> path = service.calculerCheminFiltre(start, end, sec, conf, diff);

        return path.stream()
                .map(Arc::toMap)
                .toList();
    }

    // NEW: Constrained A*
    @GetMapping("/constrained/astar")
    public List<Map<String, Object>> routeConstrainedAstar(
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "0") double sec,
            @RequestParam(defaultValue = "0") double conf,
            @RequestParam(defaultValue = "0") double diff) {

        List<Arc> path = service.calculerCheminFiltreAstar(start, end, sec, conf, diff);

        if (path.isEmpty()) {
             // Optional: Handle no path found (e.g., return 404 or empty list)
        }

        return path.stream()
                .map(Arc::toMap)
                .toList();
    }
}
