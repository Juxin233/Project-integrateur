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
	private final RestTemplate rest;
	
	public final DatabaseService service ;
	private final int DBKey = 123456;
	public DatabaseController(RestTemplate rest) {
		this.rest=rest;
		service = new DatabaseService();
	}
	
    @GetMapping("/reset")
    public String reset(@RequestParam int key) {
    	if(key==DBKey) {
    		service.reset();
    		return "Database reset successfully";
    	}else {
    		return "Authentification failed";
    	}
    }
    
    @PostMapping("/update")
    public String update() {
    	InputStream content= rest.getForObject("address to the sub graph micro service", InputStream.class);
    	service.update(content);
    	return "Database update successfully";
    }
}
