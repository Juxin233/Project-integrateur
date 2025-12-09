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

    @GetMapping("/getId/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        User e = userRepository.getUserById(id);
        if (e != null) {
            return ResponseEntity.ok(e);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/getFname/{firstName}")
    public List<User> getUserByFirstName(@PathVariable String firstName) {
        return userRepository.getUserByFirstName(firstName);
    }

    @GetMapping("/getLname/{lastName}")
    public List<User> getUserByLastName(@PathVariable String lastName) {
        return userRepository.getUserByLastName(lastName);
    }
    
    @GetMapping("/getEmail/{email}")
    public List<User> getUserByEmail(@PathVariable String email) {
        return userRepository.getUserByEmail(email);
    }
    
    //-------------------------------- POST METHODS --------------------------------//
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userRepository.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    //-------------------------------- PUT METHODS --------------------------------//
    
    @PutMapping("/replaceFname/{id}")
    public ResponseEntity<String> replaceFirstName(
            @PathVariable int id, 
            @RequestBody String firstName) {
        userRepository.replaceFirstName(id, firstName);
        return ResponseEntity.ok("Disponibilités mises à jour");
    }
    
    @PutMapping("/replaceLname/{id}")
    public ResponseEntity<String> replaceLastName(
            @PathVariable int id, 
            @RequestBody String lastName) {
        userRepository.replaceLastName(id, lastName);
        return ResponseEntity.ok("Disponibilités mises à jour");
    }
    
    @PutMapping("/replacePassword/{id}")
    public ResponseEntity<String> replacePassword(
            @PathVariable int id, 
            @RequestBody String password) {
        userRepository.replacePassword(id, password);
        return ResponseEntity.ok("Disponibilités mises à jour");
    }
    
    @PutMapping("/replaceEmail/{id}")
    public ResponseEntity<String> replaceEmail(
            @PathVariable int id, 
            @RequestBody String email) {
        userRepository.replaceEmail(id, email);
        return ResponseEntity.ok("Disponibilités mises à jour");
    }
    
    @PutMapping("/replaceProfileDef/{id}")
    public ResponseEntity<String> replaceProfileDefault(
            @PathVariable int id, 
            @RequestBody int idProfileDefault) {
        userRepository.replaceProfileDefault(id, idProfileDefault);
        return ResponseEntity.ok("Disponibilités mises à jour");
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
