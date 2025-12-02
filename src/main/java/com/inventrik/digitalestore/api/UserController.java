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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Handles user CRUD operations for tenant-specific users
 * All endpoints require authentication
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users (authenticated)")
public class UserController {

    private final UserService userService;

    @GetMapping("/api/v1/tenants/{tenantId}/users")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get users with optional filters: ?status=ACTIVE or ?username={name} or ?email={email}")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @PathVariable Integer tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {

        return ResponseEntity.ok(userService.getAllUsers(tenantId, status, username, email));
    }
    
    @GetMapping("/api/v1/tenants/{tenantId}/users/{userId}")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get a user by ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable Integer tenantId, @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(tenantId, userId));
    }

    /**
     * Get current authenticated user details
     */
    @GetMapping("/api/v1/tenants/{tenantId}/users/me")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getCurrentUser(
            @PathVariable Integer tenantId,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    /**
     * Admin creates a user (not self-registration)
     */
    @PostMapping("/api/v1/tenants/{tenantId}/users")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Admin creates a new user")
    public ResponseEntity<UserResponse> createUser(
            @PathVariable Integer tenantId,
            @Valid @RequestBody UserRequest userRequest,
            Authentication authentication) {

        String username = authentication.getName();
        UserResponse createdUser = userService.createUser(tenantId, username, userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/api/v1/tenants/{tenantId}/users/{userId}")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Update a user")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer tenantId,
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest updateRequest,
            Authentication authentication) {

        String username = authentication.getName();
        UserResponse updatedUser = userService.updateUser(tenantId, userId, username, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/api/v1/tenants/{tenantId}/users/{userId}")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer tenantId, @PathVariable Long userId) {
        userService.deleteUser(tenantId, userId);
        return ResponseEntity.noContent().build();
    }
}