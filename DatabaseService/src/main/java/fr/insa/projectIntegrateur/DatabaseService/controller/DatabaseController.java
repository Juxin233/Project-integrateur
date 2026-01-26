package fr.insa.projectIntegrateur.DatabaseService.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insa.projectIntegrateur.DatabaseService.service.DatabaseService;

@RestController
@RequestMapping("/api/route/Database")
public class DatabaseController {

	public final DatabaseService service ;

	public DatabaseController(DatabaseService service) {
		this.service = service;
	}
	
    @GetMapping("/reset")
    public String reset(@RequestParam int key) {
    	return service.reset(key);
    }
    
    @PostMapping("/update")
    public String update(@RequestParam double lonA,@RequestParam double latA,@RequestParam double lonB,@RequestParam double latB,@RequestParam int typeVoie) {
    	try {
    			service.update(lonA, latA, lonB, latB, typeVoie);
    			return "Database update successfully";
    	}catch(Exception e) {
    		return e.getMessage();
    	}
    }
}
