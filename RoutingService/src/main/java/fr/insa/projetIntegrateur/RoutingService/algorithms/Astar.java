package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.model.Graph;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
import fr.insa.projetIntegrateur.RoutingService.utils.Haversine;

import java.util.*;

public class Astar {

    public List<Noeud> shortestPath(Graph graphe, long startId, long goalId) {

        Noeud start = graphe.getNoeud(startId);
        Noeud goal = graphe.getNoeud(goalId);

        if (start == null || goal == null) return Collections.emptyList();

        // fScore = gScore + heuristique
        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, Double> fScore = new HashMap<>();
        Map<Long, Arc> cameFrom = new HashMap<>();

        for (Noeud n : graphe.getNoeuds()) {
            gScore.put(n.getId(), Double.POSITIVE_INFINITY);
            fScore.put(n.getId(), Double.POSITIVE_INFINITY);
        }

        gScore.put(startId, 0.0);
        fScore.put(startId, Haversine.distance(start.getLat(), start.getLon(), goal.getLat(), goal.getLon()));

        // PriorityQueue avec fScore
        PriorityQueue<Noeud> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> fScore.get(n.getId())));
        openSet.add(start);

        Set<Long> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            Noeud current = openSet.poll();

            if (current.getId() == goalId) {
                return reconstructPath(cameFrom, current);
            }

            closedSet.add(current.getId());

            for (Arc arc : graphe.getAdjacents(current.getId())) {
                Noeud neighbor = arc.getDestination();

                if (closedSet.contains(neighbor.getId())) continue;

                double tentativeG = gScore.get(current.getId()) + arc.getLongueur();

                if (tentativeG < gScore.get(neighbor.getId())) {
                    cameFrom.put(neighbor.getId(), arc);
                    gScore.put(neighbor.getId(), tentativeG);

                    double f = tentativeG + Haversine.distance(
                            neighbor.getLat(), neighbor.getLon(),
                            goal.getLat(), goal.getLon()
                    );
//                    f = 0.0;
                    fScore.put(neighbor.getId(), f);

                    openSet.remove(neighbor);
                    openSet.add(neighbor);
                }
            }
        
        }

        return Collections.emptyList(); // aucun chemin trouvé
    }

    private List<Noeud> reconstructPath(Map<Long, Arc> cameFrom, Noeud goal) {
    	LinkedList<Noeud> path = new LinkedList<>();
        Noeud current = goal;

        path.addFirst(current);

        while (cameFrom.containsKey(current.getId())) {
            Arc arc = cameFrom.get(current.getId());
            current = arc.getOrigine();
            path.addFirst(current);
        }

        return path;
    }
}

