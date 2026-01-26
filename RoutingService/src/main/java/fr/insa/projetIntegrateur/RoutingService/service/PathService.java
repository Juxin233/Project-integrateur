package fr.insa.projetIntegrateur.RoutingService.service;

import fr.insa.projetIntegrateur.RoutingService.model.Graph;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
import fr.insa.projetIntegrateur.RoutingService.model.Reponse;
import fr.insa.projetIntegrateur.RoutingService.config.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.utils.GeoJsonLoader;
import fr.insa.projetIntegrateur.RoutingService.utils.PostgreLoader;
import fr.insa.projetIntegrateur.RoutingService.algorithms.Astar;
import fr.insa.projetIntegrateur.RoutingService.algorithms.ConstrainedAstar;
import fr.insa.projetIntegrateur.RoutingService.algorithms.ConstrainedDijkstra;
import fr.insa.projetIntegrateur.RoutingService.algorithms.Dijkstra;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PathService {
	    private Graph graphe;
	    private final RestTemplate rest;
	    String updateUrl = "http://DatabaseService/api/route/Database/update" +
	             "?lonA={lonA}&latA={latA}&lonB={lonB}&latB={latB}&typeVoie={typeVoie}";
	    
	    @Autowired
	    public PathService(RestTemplate rest) throws Exception {
//	        this.graphe = new GeoJsonLoader().charger("toulouse_graph_nodes_edges_area_Toulouse_2025-11-27.geojson");
	    	this.graphe = PostgreLoader.loadMap();
	    	this.rest=rest;
	        if (graphe.getNombreNoeuds() > 0 && graphe.getNombreArcs() > 0) {
	            System.out.println("✅ Graph loaded successfully!");
	            System.out.printf("Nodes: %d, Arcs: %d%n", graphe.getNombreNoeuds(), graphe.getNombreArcs());
	        } else {
	            System.out.printf("⚠️ Graph loaded, but appears empty (Nodes: %d, Arcs: %d). Check GeoJSON content.%n",
	                              graphe.getNombreNoeuds(), graphe.getNombreArcs());
	        }
	    }

	    public Reponse calculerDijkstra(long start, long end,int type,int access) {
	        return new Dijkstra().shortestPath(graphe, start, end,type,access);
	    }


	    public Reponse calculerAstar(long start, long end,int type,int access) {
	        return new Astar().shortestPath(graphe, start, end,type,access);
	    }

		public Reponse calculerCheminFiltre(long start, long end,int type, int access,double sec, double conf, double diff) {
			Noeud depart = graphe.getNoeud(start);
			Noeud destination = graphe.getNoeud(end);
			String updateMessage = rest.postForObject(updateUrl, null, String.class,
					Map.of(
				            "lonA", depart.getLon(),
				            "latA", depart.getLat(),
				            "lonB", destination.getLon(),
				            "latB", destination.getLat(),
				            "typeVoie", type
				        )
			);
			System.out.println(updateMessage);
			ConstrainedDijkstra algo = new ConstrainedDijkstra();
			try {
				this.graphe=PostgreLoader.loadMap();
				System.out.println("✅ Graph loaded successfully!");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return algo.shortestPath(graphe, start, end, type, access, sec, conf, diff);
		}

        public Reponse calculerCheminFiltreAstar(long start, long end,int type,int access,double sec, double conf, double diff) {
        	Noeud depart = graphe.getNoeud(start);
			Noeud destination = graphe.getNoeud(end);
			String updateMessage = rest.postForObject(updateUrl, null, String.class,
					Map.of(
				            "lonA", depart.getLon(),
				            "latA", depart.getLat(),
				            "lonB", destination.getLon(),
				            "latB", destination.getLat(),
				            "typeVoie", type
				        )
			);
			System.out.println(updateMessage);
        	ConstrainedAstar algoAstar = new ConstrainedAstar();
        	try {
				this.graphe=PostgreLoader.loadMap();
				System.out.println("✅ Graph loaded successfully!");
			} catch (SQLException e) {
				e.printStackTrace();
			}
            return algoAstar.shortestPath(graphe, start, end, type, access,sec, conf, diff);
        }

    // --- NEW METHOD ---
    public long findNearestNode(double lat, double lon) {
        return graphe.findNearestNode(lat, lon);
    }
}


