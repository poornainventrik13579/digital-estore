package com.inventrik.digitalestore.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventrik.digitalestore.dto.request.LoginRequest;
import com.inventrik.digitalestore.dto.request.SignupRequest;
import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.service.user.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // OAuth2 login should be done through the authorization server endpoints
        // This endpoint is just for compatibility and direct authentication
        
        return ResponseEntity.ok().body("User logged in successfully");
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        // Convert SignupRequest to UserRequest
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(signupRequest.getUsername());
        userRequest.setEmail(signupRequest.getEmail());
        userRequest.setPassword(signupRequest.getPassword());
        userRequest.setFirstName(signupRequest.getFirstName());
        userRequest.setLastName(signupRequest.getLastName());
        userRequest.setPhone(signupRequest.getPhone());
        
        // Create a new user
        userService.createUser(signupRequest.getTenantId(), "system", userRequest);
        
        return ResponseEntity.ok().body("User registered successfully");
    }
}