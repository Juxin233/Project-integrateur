package fr.insa.projetIntegrateur.UserService.model;

import java.util.List;

public class Itinerary {
	//-------------------------------- FIELDS --------------------------------//
	
	//id field
	private int idItinerary;
	
	//list of nodes that make the itinerary, stored as ???
	private List<Long> itineraryNodes;
	
	//user that queried the itinerary
	private int idUser;
	
	//-------------------------------- CONSTRUCTORS --------------------------------//
	
	public Itinerary(int idItinerary, List<Long> itineraryNodes, int idUser) {
		super();
		this.idItinerary = idItinerary;
		this.itineraryNodes = itineraryNodes;
		this.idUser = idUser;
	}
	
	public Itinerary() {
	}
	
	//-------------------------------- GETTERS/SETTERS --------------------------------//
	
	public int getIdItinerary() {
		return idItinerary;
	}
	public void setIdItinerary(int idItinerary) {
		this.idItinerary = idItinerary;
	}
	public List<Long> getItineraryNodes() {
		return itineraryNodes;
	}
	public void setItineraryNodes(List<Long> itineraryNodes) {
		this.itineraryNodes = itineraryNodes;
	}
	public int getIdUser() {
		return idUser;
	}
	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}
	
}
