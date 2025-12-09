package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.model.Graph;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
import fr.insa.projetIntegrateur.RoutingService.utils.Haversine;

import java.util.*;

public class Astar {

    public List<Arc> shortestPath(Graph graphe, long startId, long goalId) {

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

                    double f = tentativeG + Haversine.distance(neighbor.getLat(), neighbor.getLon(),
                            goal.getLat(), goal.getLon());
                    fScore.put(neighbor.getId(), f);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return Collections.emptyList(); // aucun chemin trouvé
    }

    private List<Arc> reconstructPath(Map<Long, Arc> cameFrom, Noeud current) {
        LinkedList<Arc> path = new LinkedList<>();
        Long curId = current.getId();

        while (cameFrom.containsKey(curId)) {
            Arc arc = cameFrom.get(curId);
            path.addFirst(arc);
            curId = arc.getOrigine().getId();
        }

        return path;
    }
}

