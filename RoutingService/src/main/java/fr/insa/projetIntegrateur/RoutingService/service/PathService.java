package fr.insa.projetIntegrateur.RoutingService.service;

import fr.insa.projetIntegrateur.RoutingService.model.Graph;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.utils.GeoJsonLoader;
import fr.insa.projetIntegrateur.RoutingService.algorithms.Astar;
import fr.insa.projetIntegrateur.RoutingService.algorithms.Dijkstra;

@Service
public class PathService {
	    private Graph graphe;
	    
	    public PathService() throws Exception {
	        this.graphe = new GeoJsonLoader().charger("toulouse_graph_nodes_edges_area_Toulouse_2025-11-27.geojson");
	        if (graphe.getNombreNoeuds() > 0 && graphe.getNombreArcs() > 0) {
	            System.out.println("✅ Graph loaded successfully!");
	            System.out.printf("Nodes: %d, Arcs: %d%n", graphe.getNombreNoeuds(), graphe.getNombreArcs());
	        } else {
	            System.out.printf("⚠️ Graph loaded, but appears empty (Nodes: %d, Arcs: %d). Check GeoJSON content.%n", 
	                              graphe.getNombreNoeuds(), graphe.getNombreArcs());
	        }
	    }

	    public List<Arc> calculerDijkstra(long start, long end) {
	        return new Dijkstra().shortestPath(graphe, start, end);
	    }
	    
	    
	    public List<Arc> calculerAstar(long start, long end) {
	        return new Astar().shortestPath(graphe, start, end);
	    }
}
