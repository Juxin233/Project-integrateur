package fr.insa.projetIntegrateur.UserService.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import fr.insa.projetIntegrateur.UserService.model.Itinerary;
import fr.insa.projetIntegrateur.UserService.model.LoginRequest;
import fr.insa.projetIntegrateur.UserService.model.Profile;
import fr.insa.projetIntegrateur.UserService.model.User;
import fr.insa.projetIntegrateur.UserService.repository.UserRepository;

@RestController
@RequestMapping("/user")
public class UserResource {

	//-------------------------------- FIELDS & CONSTRUCTORS --------------------------------//
	
	@Autowired
    private final UserRepository userRepository;

    public UserResource(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    //-------------------------------- GET METHODS --------------------------------//

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        User e = userRepository.getUserById(id);
        if (e != null) {
            return ResponseEntity.ok(e);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/get/fName/{firstName}")
    public List<User> getUserByFirstName(@PathVariable String firstName) {
        return userRepository.getUserByFirstName(firstName);
    }

    @GetMapping("/get/lName/{lastName}")
    public List<User> getUserByLastName(@PathVariable String lastName) {
        return userRepository.getUserByLastName(lastName);
    }
    
    @GetMapping("/get/email/{email}")
    public List<User> getUserByEmail(@PathVariable String email) {
        return userRepository.getUserByEmail(email);
    }
    
    @GetMapping("/get/password/{id}")
    public String getUserPasswordById(@PathVariable int id) {
        return userRepository.getUserPasswordById(id);
    }
    
    @GetMapping("/get/profileDefault/{id}")
    public Profile getUserProfileDefaultById(@PathVariable int id) {
        return userRepository.getUserProfileDefaultById(id);
    }
    
    @GetMapping("/get/customProfile/{id}")
    public String getUserCustomProfileById(@PathVariable int id) {
        return userRepository.getUserCustomProfileById(id);
    }
    
    @GetMapping("/get/itineraries/{id}")
    public List<Itinerary> getItinerariesById(@PathVariable int id) {
        return userRepository.getItinerariesById(id);
    }
    
    /*
    @GetMapping("/get/login/{email}&{password}")
    public List<User> verifyLogin(@PathVariable String email, @PathVariable String password) {
        return userRepository.verifyLogin(email, password);
    }
    */
    
    //-------------------------------- POST METHODS --------------------------------//
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userRepository.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PostMapping("/post/itinerary")
    public ResponseEntity<Itinerary> addNewItinerary(@RequestBody Itinerary itinerary) {
    	Itinerary saved = userRepository.addNewItinerary(itinerary);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    } 
    
    @PostMapping("/login")
    public List<User> verifyLogin(@RequestBody LoginRequest loginReq) {
        return userRepository.verifyLogin(loginReq.getEmail(), loginReq.getPassword());
    }
    
    //-------------------------------- PUT METHODS --------------------------------//
    
    @PutMapping("/replace/fName/{id}")
    public ResponseEntity<String> replaceFirstName(@PathVariable int id, @RequestBody String firstName) {
    	int rows = userRepository.replaceFirstName(id, firstName);
        if (rows > 0) {
            return ResponseEntity.ok("First name replaced successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }
    
    @PutMapping("/replace/lName/{id}")
    public ResponseEntity<String> replaceLastName(@PathVariable int id, @RequestBody String lastName) {
        int rows = userRepository.replaceLastName(id, lastName);
        if (rows > 0) {
            return ResponseEntity.ok("Last name replaced successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }
    
    @PutMapping("/replace/password/{id}")
    public ResponseEntity<String> replacePassword(@PathVariable int id, @RequestBody String password) {
        int rows = userRepository.replacePassword(id, password);
        if (rows > 0) {
            return ResponseEntity.ok("Password replaced successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }
    
    @PutMapping("/replace/email/{id}")
    public ResponseEntity<String> replaceEmail(@PathVariable int id, @RequestBody String email) {
        int rows = userRepository.replaceEmail(id, email);
        if (rows > 0) {
            return ResponseEntity.ok("Email replaced successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }
    
    @PutMapping("/replace/profileDefault/{id}")
    public ResponseEntity<String> replaceProfileDefault(@PathVariable int id, @RequestBody int idProfileDefault) {
        int rows = userRepository.replaceProfileDefault(id, idProfileDefault);
        if (rows > 0) {
            return ResponseEntity.ok("Default user profile replaced successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }
    
    @PutMapping("/replace/customProfile/{id}")
    public ResponseEntity<String> replaceCustomProfile(@PathVariable int id, @RequestBody String customProfile) {
        int rows = userRepository.replaceCustomProfile(id, customProfile);
        if (rows > 0) {
            return ResponseEntity.ok("Custom user profile replaced successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }
    
    //-------------------------------- DELETE METHODS --------------------------------//
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        int rows = userRepository.deleteUser(id);
        if (rows > 0) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    
}
