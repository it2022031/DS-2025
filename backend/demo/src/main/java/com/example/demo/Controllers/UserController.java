package com.example.demo.Controllers;

import com.example.demo.Entities.User;
import com.example.demo.Services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow all origins for testing; adjust in production
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @PostMapping("/users")
    public ResponseEntity<User> addUser(@RequestBody User dto) {
        User newUser = new User();
        newUser.setName(dto.getName());
        User saved = userService.addUser(newUser);
        return ResponseEntity.ok(saved);
    }


    @PostMapping("/users/analytical")
    public ResponseEntity<User> addUserAnalytical(@RequestBody User dto) {
        User newUser = new User(
                dto.getUsername(),
                dto.getPassword(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                dto.getPassportNumber(),
                dto.getAfm()
        );
        User saved = userService.addUserAnalytical(newUser);
        return ResponseEntity.ok(saved);
    }
}

