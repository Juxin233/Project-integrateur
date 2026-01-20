package fr.insa.projectIntegrateur.DatabaseService.controller;

import java.io.InputStream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import fr.insa.projectIntegrateur.DatabaseService.service.DatabaseService;

@RestController
@RequestMapping("/api/route/Database")
public class DatabaseController {

	public final DatabaseService service ;

	public DatabaseController() {
		service = new DatabaseService();
	}
	
    @GetMapping("/reset")
    public String reset(@RequestParam int key) {
    	return service.reset(key);
    }
    
    @PostMapping("/update")
    public String update(@RequestParam double latA,@RequestParam double lonA,@RequestParam double latB,@RequestParam double lonB,@RequestParam int typeVoie) {
    	try {
    			service.update(latA, lonA, latB, lonB, typeVoie);
    			return "Database update successfully";
    	}catch(Exception e) {
    		return e.getMessage();
    	}
    }
}
