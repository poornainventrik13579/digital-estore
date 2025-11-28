package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.request.UserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
@SecurityRequirement(name = "oauth2")
public class UserController {

    private final UserService userService;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get users with optional filters: ?status=ACTIVE or ?username={name} or ?email={email}")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @PathVariable Integer tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {

        return ResponseEntity.ok(userService.getAllUsers(tenantId, status, username, email));
    }
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.isCurrentUser(#tenantId, #userId, authentication.name)")
    @Operation(summary = "Get a user by ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable Integer tenantId, @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(tenantId, userId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new user (JSON)")
    public ResponseEntity<UserResponse> createUserJson(
            @PathVariable Integer tenantId,
            @Valid @RequestBody UserRequest userRequest,
            Authentication authentication) {
        
        String username = authentication.getName();
        UserResponse createdUser = userService.createUser(tenantId, username, userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new user (Form)")
    public ResponseEntity<UserResponse> createUser(
            @PathVariable Integer tenantId,
            @Valid @ModelAttribute UserRequest userRequest,
            Authentication authentication) {
        
        String username = authentication.getName();
        UserResponse createdUser = userService.createUser(tenantId, username, userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @PutMapping(path = "/{userId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.isCurrentUser(#tenantId, #userId, authentication.name)")
    @Operation(summary = "Update a user (JSON)")
    public ResponseEntity<UserResponse> updateUserJson(
            @PathVariable Integer tenantId,
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = authentication.getName();
        UserResponse updatedUser = userService.updateUser(tenantId, userId, username, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping(path = "/{userId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.isCurrentUser(#tenantId, #userId, authentication.name)")
    @Operation(summary = "Update a user (Form)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer tenantId,
            @PathVariable Long userId,
            @Valid @ModelAttribute UserUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = authentication.getName();
        UserResponse updatedUser = userService.updateUser(tenantId, userId, username, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer tenantId, @PathVariable Long userId) {
        userService.deleteUser(tenantId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user details")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.findByUsername(username));
    }
}