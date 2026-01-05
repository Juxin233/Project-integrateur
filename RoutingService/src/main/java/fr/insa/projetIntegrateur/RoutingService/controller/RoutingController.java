package fr.insa.projetIntegrateur.RoutingService.controller;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.service.PathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RoutingController {

    @Autowired
    private PathService pathService;

    @GetMapping("/route/vectorial")
    public ResponseEntity<Map<String, List<Arc>>> getVectorialRoute(
            @RequestParam long start, 
            @RequestParam long end) {
        
        // The service now returns a Dictionary (Map) of paths
        // Key: "C1", "C2", etc. | Value: The Path (List of Arcs)
        Map<String, List<Arc>> bestPaths = pathService.calculerDijkstraVectoriel(start, end);
        
        if (bestPaths.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(bestPaths);
    }
}
