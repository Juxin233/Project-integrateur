package fr.insa.projetIntegrateur.RoutingService.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.service.PathService;

@RestController
@RequestMapping("/api/route")
public class RoutingController {

    private final PathService service;

    public RoutingController(PathService service) {
        this.service = service;
    }

    @GetMapping("/dijkstra")
    public List<Map<String,Object>> route(
            @RequestParam long start,
            @RequestParam long end) {

        return service.calculerDijkstra(start, end)
                      .stream()
                      .map(Arc::toMap)
                      .toList();
    }
}
