package com.fsalazar.springcloud.msvc.users.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fsalazar.springcloud.msvc.users.entities.User;
import com.fsalazar.springcloud.msvc.users.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> listAll() {
        logger.info("Getting all users");
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        logger.info("Getting user with id: {}", id);
        Optional<User> userOptional = userService.findById(id);
        
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User not found with id: " + id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> create(@RequestBody User user) {
        logger.info("Creating new user: {}", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody User user, @PathVariable Long id) {
        logger.info("Updating user with id: {}", id);
        Optional<User> existingUser = userService.findById(id);
        
        if (existingUser.isPresent()) {
            User updatedUser = userService.update(user, id);
            return ResponseEntity.ok(updatedUser);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User not found with id: " + id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        logger.info("Deleting user with id: {}", id);
        Optional<User> userOptional = userService.findById(id);
        
        if (userOptional.isPresent()) {
            userService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User not found with id: " + id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
