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

    @GetMapping("/get/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        User e = userRepository.getUserById(id);
        if (e != null) {
            return ResponseEntity.ok(e);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get/{lastName}")
    public List<User> getUserByLastName(@PathVariable String lastName) {
        return userRepository.getUserByLastName(lastName);
    }
    
    //-------------------------------- POST METHODS --------------------------------//
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userRepository.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    //-------------------------------- PUT METHODS --------------------------------//
    
    
    
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
