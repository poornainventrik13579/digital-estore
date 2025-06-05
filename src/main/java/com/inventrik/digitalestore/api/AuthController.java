package com.inventrik.digitalestore.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.inventrik.digitalestore.dto.request.LoginRequest;
import com.inventrik.digitalestore.dto.request.SignupRequest;
import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.security.CustomUserDetailsService;
import com.inventrik.digitalestore.security.JwtTokenService;
import com.inventrik.digitalestore.service.user.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsService userDetailsService;
    
    @PostMapping(value = "/login", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> authenticateUser(@Valid @ModelAttribute LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            String jwt = jwtTokenService.generateToken(userDetails);

            return ResponseEntity.ok(Map.of(
                "token", jwt,
                "type", "Bearer",
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }
    
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