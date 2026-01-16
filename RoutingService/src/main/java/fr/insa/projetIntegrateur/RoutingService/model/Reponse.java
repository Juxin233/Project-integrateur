package fr.insa.projetIntegrateur.RoutingService.model;

import java.util.LinkedList;
import java.util.List;

public class Reponse {
	private List<Noeud> list;
	private double confort;
	private double diff;
	private double risque;
	private int typeVoie;
	private boolean profil_change;
	
	public Reponse(List<Noeud> list, double confort,double diff,double risque, int typeVoie,boolean profil_change) {
		this.list=list;
		this.confort= confort;
		this.diff=diff;
		this.risque=risque;
		this.typeVoie=typeVoie;
		this.profil_change=profil_change;
	}
	
	public List<Noeud> getList() {
		return this.list;
	}

	public double getConfort() {
		return confort;
	}

	public double getDiff() {
		return diff;
	}

	public double getRisque() {
		return risque;
	}

	public int getTypeVoie() {
		return typeVoie;
	}

	public boolean isProfil_change() {
		return profil_change;
	}
	
}
