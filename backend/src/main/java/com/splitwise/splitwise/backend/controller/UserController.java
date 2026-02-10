package com.splitwise.splitwise.backend.controller;

import com.splitwise.splitwise.backend.entity.User;
import com.splitwise.splitwise.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    //Create a user
    @PostMapping
    public User create(@RequestBody User user){
        user.setId(null); //ensure new insert
        return userRepository.save(user);
    }

    // List of all users
    @GetMapping
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
}
