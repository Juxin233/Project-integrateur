package fr.insa.projetIntegrateur.RoutingService.algorithms;

import fr.insa.projetIntegrateur.RoutingService.model.Arc;
import fr.insa.projetIntegrateur.RoutingService.model.Graph;
import fr.insa.projetIntegrateur.RoutingService.model.Noeud;
import fr.insa.projetIntegrateur.RoutingService.model.Reponse;
import fr.insa.projetIntegrateur.RoutingService.utils.Haversine;

import java.util.*;

public class Astar {

    public Reponse shortestPath(Graph graphe, long startId, long goalId, int type,int access) {

        Noeud start = graphe.getNoeud(startId);
        Noeud goal = graphe.getNoeud(goalId);

        if (start == null || goal == null) return new Reponse(Collections.emptyList(),0,0,0,type,false);

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
                return reconstructPath(cameFrom, current,type);
            }

            closedSet.add(current.getId());

            for (Arc arc : graphe.getAdjacents(current.getId())) {
            	int T =arc.getType_route();
            	if (T != type && T !=2 ) continue; 
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

        return new Reponse(Collections.emptyList(),0,0,0,type,false); // aucun chemin trouvé
    }

    private Reponse reconstructPath(Map<Long, Arc> cameFrom, Noeud goal,int type) {
    	LinkedList<Noeud> path = new LinkedList<>();
        Noeud current = goal;

        path.addFirst(current);
        double confort=0.0;
        double diff=0.0;
        double risque=0.0;
        double counter=0;
        while (cameFrom.containsKey(current.getId())) {
            Arc arc = cameFrom.get(current.getId());
            current = arc.getOrigine();
            path.addFirst(current);
            switch (type) {
        	case 0:
        		confort += arc.getConfortPieton();
        		diff += arc.getDiffPieton();
        		risque += arc.getRisquePieton();
        		break;
        	case 1:
        		confort += arc.getConfortVelo();
        		diff += arc.getDiffVelo();
        		risque += arc.getRisqueVelo();
        		break;
        	case 2:
        		confort += arc.getConfortPieton();
        		diff += arc.getConfortPieton();
        		risque += arc.getRisquePieton();
        		break;
            }
            counter ++;
        }
        confort = confort / counter;
        diff = diff/counter;
        risque = risque/counter;
        return new Reponse(path,confort,diff,risque,type,false);
    }
}

