package fr.insa.projetIntegrateur.RoutingService.service;

import fr.insa.projetIntegrateur.RoutingService.model.Graph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.utils.GeoJsonLoader;
import fr.insa.projetIntegrateur.RoutingService.algorithms.Astar;
import fr.insa.projetIntegrateur.RoutingService.algorithms.Dijkstra;
import fr.insa.projetIntegrateur.RoutingService.algorithms.VectorialDijkstra;
import fr.insa.projetIntegrateur.RoutingService.model.VectorCost;

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

		// In PathService.java

		public Map<String, List<Arc>> calculerDijkstraVectoriel(long start, long end) {
				VectorialDijkstra algo = new VectorialDijkstra();
				
				// 1. Get ALL non-dominated trade-off paths
				List<List<Arc>> paretoPaths = algo.findParetoPaths(graphe, start, end);

				if (paretoPaths.isEmpty()) {
					return Collections.emptyMap();
				}

				// 2. Prepare to find the best path for each component
				List<Arc> bestC1Path = null;
				List<Arc> bestC2Path = null;
				List<Arc> bestC3Path = null;
				List<Arc> bestC4Path = null;

				double minC1 = Double.MAX_VALUE;
				double minC2 = Double.MAX_VALUE;
				double minC3 = Double.MAX_VALUE;
				double minC4 = Double.MAX_VALUE;

				// 3. Iterate through every path to find the extremes
				for (List<Arc> path : paretoPaths) {
					VectorCost pathCost = calculatePathCost(path);

					if (pathCost.c1 < minC1) {
						minC1 = pathCost.c1;
						bestC1Path = path;
					}
					if (pathCost.c2 < minC2) {
						minC2 = pathCost.c2;
						bestC2Path = path;
					}
					if (pathCost.c3 < minC3) {
						minC3 = pathCost.c3;
						bestC3Path = path;
					}
					if (pathCost.c4 < minC4) {
						minC4 = pathCost.c4;
						bestC4Path = path;
					}
				}

				// 4. Construct the dictionary (Map)
				Map<String, List<Arc>> result = new LinkedHashMap<>();
				
				// We only add the entry if a path was found (which should be true if paretoPaths is not empty)
				if (bestC1Path != null) result.put("C1", bestC1Path); // e.g., Shortest
				if (bestC2Path != null) result.put("C2", bestC2Path); // e.g., Fastest
				if (bestC3Path != null) result.put("C3", bestC3Path); // e.g., Safest
				if (bestC4Path != null) result.put("C4", bestC4Path); // e.g., Most Comfortable

				return result;
			}

			// Helper to sum up the vector cost of a path
			private VectorCost calculatePathCost(List<Arc> path) {
				VectorCost total = VectorCost.zero();
				for (Arc arc : path) {
					total = total.add(extractCostFromArc(arc));
				}
				return total;
			}

			// Ensure this matches the logic inside VectorialDijkstra
			private VectorCost extractCostFromArc(Arc arc) {
				double c1 = arc.getLongueur();
				double c2 = arc.getTempsMarche();
				double c3 = arc.getRisquePieton();
				double c4 = (10.0 - arc.getConfortPieton());
				return new VectorCost(c1, c2, c3, c4);
			}
		}
