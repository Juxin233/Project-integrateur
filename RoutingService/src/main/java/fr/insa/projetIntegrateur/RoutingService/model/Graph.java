package fr.insa.projetIntegrateur.RoutingService.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
	 	private final Map<Long, Noeud> noeuds = new HashMap<>();
	    private final Map<Long, List<Arc>> adj = new HashMap<>();

	    public void ajouterNoeud(Noeud n) {
	        noeuds.put(n.getId(), n);
	        adj.putIfAbsent(n.getId(), new ArrayList<>());
	    }

	    public void ajouterArc(Arc arc) {
	        adj.get(arc.getOrigine().getId()).add(arc);
	    }

	    public Noeud getNoeud(long id) { return noeuds.get(id); }

	    public List<Arc> getAdjacents(long id) {
	        return adj.getOrDefault(id, List.of());
	    }

	    public Collection<Noeud> getNoeuds() { return noeuds.values(); }
	    
	    
	 // Nouvelle méthode pour obtenir le nombre total de noeuds
        public int getNombreNoeuds() {
            return noeuds.size();
        }

        // Nouvelle méthode pour obtenir le nombre total d'arcs
        public int getNombreArcs() {
            // Calculate the total number of arcs by summing the size of all adjacency lists
            return adj.values().stream()
                       .mapToInt(List::size)
                       .sum();
        }
}
