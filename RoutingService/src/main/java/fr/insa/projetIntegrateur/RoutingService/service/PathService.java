package fr.insa.projetIntegrateur.RoutingService.service;

import fr.insa.projetIntegrateur.RoutingService.model.Graph;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
import fr.insa.projetIntegrateur.RoutingService.utils.GeoJsonLoader;
import fr.insa.projetIntegrateur.RoutingService.algorithms.Astar;
import fr.insa.projetIntegrateur.RoutingService.algorithms.ConstrainedAstar;
import fr.insa.projetIntegrateur.RoutingService.algorithms.ConstrainedDijkstra;
import fr.insa.projetIntegrateur.RoutingService.algorithms.Dijkstra;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PathService {
    private Graph graphe;

    // Wrapper class to hold the path and the relaxation flag
    public static class PathResult {
        public List<Noeud> path;
        public boolean constraintsRelaxed;

        public PathResult(List<Noeud> path, boolean constraintsRelaxed) {
            this.path = path;
            this.constraintsRelaxed = constraintsRelaxed;
        }
    }

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

    public List<Noeud> calculerDijkstra(long start, long end, int type) {
        return new Dijkstra().shortestPath(graphe, start, end, type);
    }

    public List<Noeud> calculerAstar(long start, long end, int type) {
        return new Astar().shortestPath(graphe, start, end, type);
    }

    // Updated to return PathResult
    public PathResult calculerCheminFiltre(long start, long end, int type, double sec, double conf, double diff) {
        ConstrainedDijkstra algo = new ConstrainedDijkstra();
        return algo.shortestPath(graphe, start, end, type, sec, conf, diff);
    }

    // Updated to return PathResult
    public PathResult calculerCheminFiltreAstar(long start, long end, int type, double sec, double conf, double diff) {
        ConstrainedAstar algoAstar = new ConstrainedAstar();
        return algoAstar.shortestPath(graphe, start, end, type, sec, conf, diff);
    }
}