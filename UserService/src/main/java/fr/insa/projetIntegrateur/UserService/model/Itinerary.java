package fr.insa.projetIntegrateur.UserService.model;

import java.util.List;

public class Itinerary {
	//-------------------------------- FIELDS --------------------------------//
	
	//id field
	private Integer idItinerary;
	
	//itinerary returned by the routing service, stored as a json in the db
	private String itineraryCol;
	
	//user that queried the itinerary
	private Integer idUser;
	
	//-------------------------------- CONSTRUCTORS --------------------------------//
	
	public Itinerary(Integer idItinerary, String itineraryCol, Integer idUser) {
		this.idItinerary = idItinerary;
		this.itineraryCol = itineraryCol;
		this.idUser = idUser;
	}
	
	public Itinerary() {
	}
	
	//-------------------------------- GETTERS/SETTERS --------------------------------//
	
	public Integer getIdItinerary() {
		return idItinerary;
	}
	public void setIdItinerary(Integer idItinerary) {
		this.idItinerary = idItinerary;
	}
	public String getItineraryCol() {
		return itineraryCol;
	}
	public void setItineraryCol(String itineraryCol) {
		this.itineraryCol = itineraryCol;
	}
	public Integer getIdUser() {
		return idUser;
	}
	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}
	
}
