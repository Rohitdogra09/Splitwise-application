package com.splitwise.splitwise.backend.auth;

import com.splitwise.splitwise.backend.entity.User;
import com.splitwise.splitwise.backend.repository.UserRepository;
import com.splitwise.splitwise.backend.secruity.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    //Register
    @PostMapping("/register")
    public  String register(@RequestBody RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return "User registered successfully";

    }

    //Login
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request){

        User user =userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw  new RuntimeException("Invalid password");
        }
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

}
