package com.inventrik.digitalestore.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.http.MediaType;

import com.inventrik.digitalestore.dto.request.SignupRequest;
import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.service.user.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    
    @PostMapping(value = "/signup", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> registerUser(@Valid @ModelAttribute SignupRequest signupRequest) {
        try {
            UserRequest userRequest = new UserRequest();
            userRequest.setUsername(signupRequest.getUsername());
            userRequest.setEmail(signupRequest.getEmail());
            userRequest.setPassword(signupRequest.getPassword());
            userRequest.setFirstName(signupRequest.getFirstName());
            userRequest.setLastName(signupRequest.getLastName());
            userRequest.setPhone(signupRequest.getPhone());
            
            userService.createUser(signupRequest.getTenantId(), "system", userRequest);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}