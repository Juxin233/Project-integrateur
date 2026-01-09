package fr.insa.projetIntegrateur.UserService.model;

import java.util.List;

public class Itinerary {
	//-------------------------------- FIELDS --------------------------------//
	
	//id field
	private int idItinerary;
	
	//itinerary returned by the routing service, stored as a json in the db
	private String itineraryCol;
	
	//user that queried the itinerary
	private int idUser;
	
	//-------------------------------- CONSTRUCTORS --------------------------------//
	
	public Itinerary(int idItinerary, String itineraryCol, int idUser) {
		this.idItinerary = idItinerary;
		this.itineraryCol = itineraryCol;
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
	public String getItineraryCol() {
		return itineraryCol;
	}
	public void setItineraryCol(String itineraryCol) {
		this.itineraryCol = itineraryCol;
	}
	public int getIdUser() {
		return idUser;
	}
	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}
	
}
