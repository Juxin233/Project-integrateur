package fr.insa.projetIntegrateur.GatewayService.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import fr.insa.projetIntegrateur.GatewayService.model.*;
//import fr.insa.projetIntegrateur.UserService.model.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/gateway")
public class GatewayServiceResource {

	//=================================================
    // PARAMETERS + CONSTRUCTOR
    //=================================================

    private final RestTemplate restTemplate;
    private final String USER_MS_URL = "http://UserService/user";
    private final String ROUTING_MS_URL = "http://RoutingService/routing";
    private final String DATABASE_MS_URL = "http://DatabaseService/database";
    //private final String GUI_URL = "http://GUI/gui";
    
    @Autowired
    public GatewayServiceResource(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    //=================================================
    // USERSERVICE
    //=================================================
    
    //========================GET METHODS=========================
    
    @GetMapping("/user")
    public ResponseEntity<String> getAllUsers() {
    	try {
            ObjectMapper mapper = new ObjectMapper();

            //fetch all users
            String rawUsersJson = restTemplate.getForObject(USER_MS_URL, String.class);
            List<Map<String, Object>> users = mapper.readValue(rawUsersJson, new TypeReference<>(){});

            String prettyJson = getPrettyOutput(mapper.writeValueAsString(users));
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prettyJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during users query from database.\"}");
        }
    }
    
    @GetMapping("/user/get/{id}")
    public ResponseEntity<String> getUserById(@PathVariable int id) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Fetch the user
            String rawJson = restTemplate.getForObject(USER_MS_URL + "/get/" + id, String.class);
            Map<String, Object> user = mapper.readValue(rawJson, new TypeReference<>(){});

            String prettyJson = getPrettyOutput(mapper.writeValueAsString(user));
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prettyJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during user query (by ID) from database.\"}");
        }
    }
    
    @GetMapping("/user/get/fName/{firstName}")
    public ResponseEntity<String> getUserByFirstName(@PathVariable String firstName) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Fetch users
            String rawJson = restTemplate.getForObject(USER_MS_URL + "/get/fName/" + firstName, String.class);
            List<Map<String, Object>> users = mapper.readValue(rawJson, new TypeReference<>(){});

            String prettyJson = getPrettyOutput(mapper.writeValueAsString(users));
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prettyJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during users query (by first name) from database.\"}");
        }
    }
    
    @GetMapping("/user/get/lName/{lastName}")
    public ResponseEntity<String> getUserByLastName(@PathVariable String lastName) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Fetch users
            String rawJson = restTemplate.getForObject(USER_MS_URL + "/get/lName/" + lastName, String.class);
            List<Map<String, Object>> users = mapper.readValue(rawJson, new TypeReference<>(){});

            String prettyJson = getPrettyOutput(mapper.writeValueAsString(users));
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prettyJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during users query (by last name) from database.\"}");
        }
    }
    
    @GetMapping("/user/get/email/{email}")
    public ResponseEntity<String> getUserByEmail(@PathVariable String email) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Fetch users
            String rawJson = restTemplate.getForObject(USER_MS_URL + "/get/email/" + email, String.class);
            List<Map<String, Object>> users = mapper.readValue(rawJson, new TypeReference<>(){});

            String prettyJson = getPrettyOutput(mapper.writeValueAsString(users));
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prettyJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during user query (by email) from database.\"}");
        }
    }
    
    @GetMapping("/user/get/password/{id}")
    public ResponseEntity<String> getUserPasswordById(@PathVariable int id) {
        try {
            // Fetch raw string (password)
            String password = restTemplate.getForObject(USER_MS_URL + "/get/password/" + id, String.class);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"password\":\"" + password + "\"}");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during password query from database.\"}");
        }
    }
    
    @GetMapping("/user/get/profileDefault/{id}")
    public ResponseEntity<String> getUserProfileDefaultById(@PathVariable int id) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Fetch profile
            String rawJson = restTemplate.getForObject(USER_MS_URL + "/get/profileDefault/" + id, String.class);
            Map<String, Object> profile = mapper.readValue(rawJson, new TypeReference<>(){});

            String prettyJson = getPrettyOutput(mapper.writeValueAsString(profile));
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prettyJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during profile query from database.\"}");
        }
    }
    
    @GetMapping("/user/get/customProfile/{id}")
    public ResponseEntity<String> getUserCustomProfileById(@PathVariable int id) {
        try {
            // Fetch custom profile string
            String customProfile = restTemplate.getForObject(USER_MS_URL + "/get/customProfile/" + id, String.class);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"customProfile\":" + customProfile + "}");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during custom profile query from database.\"}");
        }
    }
    
    @GetMapping("/user/get/itineraries/{id}")
    public ResponseEntity<String> getItinerariesById(@PathVariable int id) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Fetch itineraries
            String rawJson = restTemplate.getForObject(USER_MS_URL + "/get/itineraries/" + id, String.class);
            List<Map<String, Object>> itineraries = mapper.readValue(rawJson, new TypeReference<>(){});

            String prettyJson = getPrettyOutput(mapper.writeValueAsString(itineraries));
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prettyJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during itineraries query from database.\"}");
        }
    }
    
    //========================POST METHODS=========================
    
    @PostMapping("/user")
    public ResponseEntity<String> createUser(@RequestBody Map<String, Object> user) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // fetch saved user
            String rawJson = restTemplate.postForObject(USER_MS_URL, user, String.class);
            Map<String, Object> saved = mapper.readValue(rawJson, new TypeReference<>(){});

            String prettyJson = getPrettyOutput(mapper.writeValueAsString(saved));
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prettyJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during user creation in database.\"}");
        }
    }
    
    @PostMapping("/user/post/itinerary")
    public ResponseEntity<String> addNewItinerary(@RequestBody Map<String, Object> itinerary) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // fetch saved itinerary - Target URL includes /post/
            String rawJson = restTemplate.postForObject(USER_MS_URL + "/post/itinerary", itinerary, String.class);
            Map<String, Object> saved = mapper.readValue(rawJson, new TypeReference<>(){});

            String prettyJson = getPrettyOutput(mapper.writeValueAsString(saved));
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prettyJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during itinerary creation in database.\"}");
        }
    }
    
    @PostMapping("/user/login")
    public ResponseEntity<String> verifyLogin(@RequestBody Map<String, Object> loginReq) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // fetch login result
            String rawJson = restTemplate.postForObject(USER_MS_URL + "/login", loginReq, String.class);
            List<Map<String, Object>> users = mapper.readValue(rawJson, new TypeReference<>(){});

            String prettyJson = getPrettyOutput(mapper.writeValueAsString(users));
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(prettyJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during login verification.\"}");
        }
    }
    
    //========================PUT METHODS=========================
    @PutMapping("/user/replace/fName/{id}")
    public ResponseEntity<String> replaceFirstName(@PathVariable int id, @RequestBody String firstName) {
        try {
            // We use exchange instead of put to see the response status
            HttpEntity<String> requestEntity = new HttpEntity<>(firstName);
            ResponseEntity<String> response = restTemplate.exchange(
                USER_MS_URL + "/replace/fName/" + id,
                HttpMethod.PUT,
                requestEntity,
                String.class
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response.getBody());

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            // This catches the 404 from the User Service
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"User with ID " + id + " not found.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during first name update.\"}");
        }
    }
    
    @PutMapping("/user/replace/lName/{id}")
    public ResponseEntity<String> replaceLastName(@PathVariable int id, @RequestBody String lastName) {
        try {
            HttpEntity<String> requestEntity = new HttpEntity<>(lastName);
            ResponseEntity<String> response = restTemplate.exchange(
                USER_MS_URL + "/replace/lName/" + id,
                HttpMethod.PUT,
                requestEntity,
                String.class
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response.getBody());

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"User not found.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during last name update in database.\"}");
        }
    }

    @PutMapping("/user/replace/password/{id}")
    public ResponseEntity<String> replacePassword(@PathVariable int id, @RequestBody String password) {
        try {
            HttpEntity<String> requestEntity = new HttpEntity<>(password);
            ResponseEntity<String> response = restTemplate.exchange(
                USER_MS_URL + "/replace/password/" + id,
                HttpMethod.PUT,
                requestEntity,
                String.class
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response.getBody());

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"User not found.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during password update in database.\"}");
        }
    }

    @PutMapping("/user/replace/email/{id}")
    public ResponseEntity<String> replaceEmail(@PathVariable int id, @RequestBody String email) {
        try {
            HttpEntity<String> requestEntity = new HttpEntity<>(email);
            ResponseEntity<String> response = restTemplate.exchange(
                USER_MS_URL + "/replace/email/" + id,
                HttpMethod.PUT,
                requestEntity,
                String.class
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response.getBody());

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"User not found.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during email update in database.\"}");
        }
    }

    @PutMapping("/user/replace/profileDefault/{id}")
    public ResponseEntity<String> replaceProfileDefault(@PathVariable int id, @RequestBody int idProfileDefault) {
        try {
            HttpEntity<Integer> requestEntity = new HttpEntity<>(idProfileDefault);
            ResponseEntity<String> response = restTemplate.exchange(
                USER_MS_URL + "/replace/profileDefault/" + id,
                HttpMethod.PUT,
                requestEntity,
                String.class
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response.getBody());

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"User not found.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during default profile update in database.\"}");
        }
    }

    @PutMapping("/user/replace/customProfile/{id}")
    public ResponseEntity<String> replaceCustomProfile(@PathVariable int id, @RequestBody String customProfile) {
        try {
            HttpEntity<String> requestEntity = new HttpEntity<>(customProfile);
            ResponseEntity<String> response = restTemplate.exchange(
                USER_MS_URL + "/replace/customProfile/" + id,
                HttpMethod.PUT,
                requestEntity,
                String.class
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response.getBody());

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"User not found.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during custom profile update in database.\"}");
        }
    }
    
    //========================DELETE METHODS=========================
    
    @DeleteMapping("/user/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        try {
            // We use exchange to capture the status code (204 or 404) from the UserService
            ResponseEntity<Void> response = restTemplate.exchange(
                USER_MS_URL + "/delete/" + id,
                HttpMethod.DELETE,
                null,
                Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"User not found.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during user deletion from database.\"}");
        }
    }
    
    //=================================================
    // ROUTINGSERVICE
    //=================================================
    
    @GetMapping("/route/dijkstra")
    public ResponseEntity<String> getDijkstraPath(@RequestParam long start, @RequestParam long end) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String url = ROUTING_MS_URL + "/api/route/dijkstra?start=" + start + "&end=" + end;

            String rawJson = restTemplate.getForObject(url, String.class);
            List<Map<String, Object>> path = mapper.readValue(rawJson, new TypeReference<>(){});

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(getPrettyOutput(mapper.writeValueAsString(path)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during Dijkstra path calculation.\"}");
        }
    }

    @GetMapping("/route/astar")
    public ResponseEntity<String> getAstarPath(@RequestParam long start, @RequestParam long end) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String url = ROUTING_MS_URL + "/api/route/astar?start=" + start + "&end=" + end;

            String rawJson = restTemplate.getForObject(url, String.class);
            List<Map<String, Object>> path = mapper.readValue(rawJson, new TypeReference<>(){});

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(getPrettyOutput(mapper.writeValueAsString(path)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during A* path calculation.\"}");
        }
    }

    @GetMapping("/route/constrained")
    public ResponseEntity<String> getConstrainedPath(
            @RequestParam long start, @RequestParam long end,
            @RequestParam(defaultValue = "0") double sec,
            @RequestParam(defaultValue = "0") double conf,
            @RequestParam(defaultValue = "0") double diff) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String url = ROUTING_MS_URL + "/api/route/constrained?start=" + start + "&end=" + end 
                       + "&sec=" + sec + "&conf=" + conf + "&diff=" + diff;

            String rawJson = restTemplate.getForObject(url, String.class);
            List<Map<String, Object>> path = mapper.readValue(rawJson, new TypeReference<>(){});

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(getPrettyOutput(mapper.writeValueAsString(path)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during constrained path calculation.\"}");
        }
    }

    @GetMapping("/route/constrained/astar")
    public ResponseEntity<String> getConstrainedAstarPath(
            @RequestParam long start, @RequestParam long end,
            @RequestParam(defaultValue = "0") double sec,
            @RequestParam(defaultValue = "0") double conf,
            @RequestParam(defaultValue = "0") double diff) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String url = ROUTING_MS_URL + "/api/route/constrained/astar?start=" + start + "&end=" + end 
                       + "&sec=" + sec + "&conf=" + conf + "&diff=" + diff;

            String rawJson = restTemplate.getForObject(url, String.class);
            List<Map<String, Object>> path = mapper.readValue(rawJson, new TypeReference<>(){});

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(getPrettyOutput(mapper.writeValueAsString(path)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during constrained A* path calculation.\"}");
        }
    }
    
    //=================================================
    // DATABASESERVICE
    //=================================================
    
    @GetMapping("/database/reset")
    public ResponseEntity<String> resetDatabase(@RequestParam int key) {
        try {
            // Target URL: http://localhost:PORT/api/route/Database/reset?key=123456
            String url = DATABASE_MS_URL + "/api/route/Database/reset?key=" + key;
            
            String response = restTemplate.getForObject(url, String.class);
            
            if ("Database reset successfully".equals(response)) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\":\"" + response + "\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during database reset.\"}");
        }
    }

    @PostMapping("/database/update")
    public ResponseEntity<String> updateDatabase() {
        try {
            // Target URL: http://localhost:PORT/api/route/Database/update
            // Since the controller method doesn't take a body, we pass null
            String response = restTemplate.postForObject(DATABASE_MS_URL + "/api/route/Database/update", null, String.class);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error during database update.\"}");
        }
    }
    
    //=================================================
    // UTILITY METHODS
    //=================================================
    
    private String getPrettyOutput(String rawJson) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
	        JsonNode jsonTree = mapper.readTree(rawJson);
	        String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonTree);
	        return prettyJson;
        } catch (Exception e) {
        	System.out.println("Error improving json display, printing raw Json instead.");
        	return rawJson;
        }    	
    }
    
}
