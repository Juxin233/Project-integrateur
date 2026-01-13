package fr.insa.projectIntegrateur.DatabaseService.service;

import java.io.InputStream;

import org.springframework.stereotype.Service;

import fr.insa.projectIntegrateur.DatabaseService.utils.*;

@Service
public class DatabaseService {

	public DatabaseService() {
	}
	
	public void reset() {
		String[] args = {};
		try {
			GeoJsonToPostgresImporter.main(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void update(InputStream content) {
		
		try {
			System.out.println(PostgreUpdate.updateFromGeoJson(content));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
