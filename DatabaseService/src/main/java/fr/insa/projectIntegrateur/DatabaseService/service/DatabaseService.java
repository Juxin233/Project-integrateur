package fr.insa.projectIntegrateur.DatabaseService.service;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import fr.insa.projectIntegrateur.DatabaseService.model.CorridorRequest;
import fr.insa.projectIntegrateur.DatabaseService.utils.*;
import fr.insa.projectIntegrateur.DatabaseService.config.*;

@Service
public class DatabaseService {
	private final int DBKey = 123456;
	
	private RestTemplate rest;
	
	public DatabaseService() {
		rest = new RestTemplate();
	}
	
	public String reset(int key) {
		if(key==DBKey) {
			String[] args = {};
			try {
				GeoJsonToPostgresImporter.main(args);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return "Database reset successfully";
    	}else {
    		return "Authentification failed";
    	}
		
	}
	
	public void update(double lonA,double latA,double lonB,double latB,int typeVoie) {
		String urlPieton = "http://192.168.37.125:50002/api/edges/ellipse"+"?lonA={lonA}&latA={latA}&lonB={lonB}&latB={latB}";
		String urlVelo = "http://192.168.37.190/arcs/corridor";
		switch (typeVoie) {
		case 0:
	    	byte[] bodyPieton = rest.getForObject(urlPieton, byte[].class,Map.of(
	    			"lonA",lonA,
	    			"latA",latA,
	    			"lonB",lonB,
	    			"latB",latB
	    			)
	    		);
	    	if(bodyPieton ==null) {
	    		throw new IllegalStateException("Empty response body from sub graph service");
	    	}
	    	try {
	    		InputStream contentPieton = new ByteArrayInputStream(bodyPieton);
				PostgreUpdate.updateFromJson(contentPieton,0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case 1:
			 CorridorRequest demandBody = new CorridorRequest(
		                lonA, latA,
		                lonB, latB,
		                2000,
		                150000
		        );

		        HttpHeaders headers = new HttpHeaders();
		        headers.setContentType(MediaType.APPLICATION_JSON);
		        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		        HttpEntity<CorridorRequest> entity =
		                new HttpEntity<>(demandBody, headers);

		        ResponseEntity<byte[]> response =
		                rest.postForEntity(
		                        urlVelo,
		                        entity,
		                        byte[].class
		                );

		        byte[] bodyVelo = response.getBody();
		        
		    	if(bodyVelo ==null) {
		    		throw new IllegalStateException("Empty response body from sub graph service");
		    	}
		    	try {
		    		InputStream contentPieton = new ByteArrayInputStream(bodyVelo);
					PostgreUpdate.updateFromJson(contentPieton,1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		default :
			byte[] body = rest.getForObject(urlPieton, byte[].class,Map.of(
	    			"lonA",lonA,
	    			"latA",latA,
	    			"lonB",lonB,
	    			"latB",latB
	    			)
	    		);
	    	if(body ==null) {
	    		throw new IllegalStateException("Empty response body from sub graph service");
	    	}
	    	try {
	    		InputStream contentPieton = new ByteArrayInputStream(body);
				PostgreUpdate.updateFromJson(contentPieton,0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
	}
	
}
