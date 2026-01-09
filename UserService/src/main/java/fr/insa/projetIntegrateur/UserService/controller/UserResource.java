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
    
    @GetMapping("/get/profileDefault/{idUser}")
    public Profile getUserProfileDefaultById(@PathVariable int id) {
        return userRepository.getUserProfileDefaultById(id);
    }
    
    @GetMapping("/get/customProfile/{idUser}")
    public String getUserCustomProfileById(@PathVariable int id) {
        return userRepository.getUserCustomProfileById(id);
    }
    
    @GetMapping("/get/Itineraries/{idUser}")
    public List<Itinerary> getItinerariesById(@PathVariable int id) {
        return userRepository.getItinerariesById(id);
    }
    
    //-------------------------------- POST METHODS --------------------------------//
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userRepository.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PostMapping("post/itinerary")
    public ResponseEntity<Itinerary> addNewItinerary(@RequestBody Itinerary itinerary) {
    	Itinerary saved = userRepository.addNewItinerary(itinerary);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    } 
    
    //-------------------------------- PUT METHODS --------------------------------//
    
    @PutMapping("/replace/fName/{id}")
    public ResponseEntity<String> replaceFirstName(
            @PathVariable int id, 
            @RequestBody String firstName) {
        userRepository.replaceFirstName(id, firstName);
        return ResponseEntity.ok("First name replaced successfully.");
    }
    
    @PutMapping("/replace/lName/{id}")
    public ResponseEntity<String> replaceLastName(
            @PathVariable int id, 
            @RequestBody String lastName) {
        userRepository.replaceLastName(id, lastName);
        return ResponseEntity.ok("Last name replaced successfully.");
    }
    
    @PutMapping("/replace/password/{id}")
    public ResponseEntity<String> replacePassword(
            @PathVariable int id, 
            @RequestBody String password) {
        userRepository.replacePassword(id, password);
        return ResponseEntity.ok("Password replaced successfully.");
    }
    
    @PutMapping("/replace/email/{id}")
    public ResponseEntity<String> replaceEmail(
            @PathVariable int id, 
            @RequestBody String email) {
        userRepository.replaceEmail(id, email);
        return ResponseEntity.ok("Email replaced successfully.");
    }
    
    @PutMapping("/replace/profileDef/{id}")
    public ResponseEntity<String> replaceProfileDefault(
            @PathVariable int id, 
            @RequestBody int idProfileDefault) {
        userRepository.replaceProfileDefault(id, idProfileDefault);
        return ResponseEntity.ok("Default user profile replaced successfully.");
    }
    
    @PutMapping("/replace/customProfile/{id}")
    public ResponseEntity<String> replaceCustomProfile(
            @PathVariable int id, 
            @RequestBody String customProfile) {
        userRepository.replaceCustomProfile(id, customProfile);
        return ResponseEntity.ok("Custom user profile replaced successfully.");
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
