package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.request.UserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    @PreAuthorize("hasAuthority('SCOPE_read') and hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Get all users", 
        description = "Retrieves all users for the specified tenant. Requires ADMIN role and read scope.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
        }
    )
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(userService.getAllUsers(tenantId));
    }
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('SCOPE_read') and (hasRole('ROLE_ADMIN') or @userService.isCurrentUser(#tenantId, #userId, authentication.name))")
    @Operation(
        summary = "Get a user by ID",
        description = "Retrieves a specific user by their ID. Users can only access their own data unless they have ADMIN role.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
        }
    )
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "User ID", required = true) 
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(tenantId, userId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority('SCOPE_write') and hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Create a new user",
        description = "Creates a new user with the provided details. Requires ADMIN role and write scope." +
                "Username must be 3-50 characters. " +
                "Email must be valid and under 100 characters. " +
                "Phone must be under 100 characters. " +
                "Password must be 8-100 characters.",
        responses = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
        }
    )
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "User details", required = true, 
                    schema = @Schema(implementation = UserRequest.class))
            @Valid @RequestBody UserRequest userRequest,
            Authentication authentication) {
        
        // Get username from authentication
        String username = authentication.getName();
        
        UserResponse createdUser = userService.createUser(tenantId, username, userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @PutMapping(path = "/{userId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority('SCOPE_write') and (hasRole('ROLE_ADMIN') or @userService.isCurrentUser(#tenantId, #userId, authentication.name))")
    @Operation(
        summary = "Update a user",
        description = "Updates an existing user with the provided details. Users can only update their own data unless they have ADMIN role." +
                "First name must be under 50 characters. " +
                "Last name must be under 50 characters. " +
                "Email must be valid and under 100 characters. " +
                "Phone must be under 100 characters.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
        }
    )
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "User ID", required = true) 
            @PathVariable Long userId,
            @Parameter(description = "User update details", required = true, 
                    schema = @Schema(implementation = UserUpdateRequest.class))
            @Valid @RequestBody UserUpdateRequest updateRequest,
            Authentication authentication) {
        
        // Get username from authentication
        String username = authentication.getName();
        
        UserResponse updatedUser = userService.updateUser(tenantId, userId, username, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('SCOPE_write') and hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Delete a user",
        description = "Deletes a user by their ID. Requires ADMIN role and write scope.",
        responses = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
        }
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "User ID", required = true) 
            @PathVariable Long userId) {
        userService.deleteUser(tenantId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('SCOPE_read') and hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Get active users",
        description = "Retrieves all active users for the specified tenant. Requires ADMIN role and read scope.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of active users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
        }
    )
    public ResponseEntity<List<UserResponse>> getActiveUsers(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(userService.getActiveUsers(tenantId));
    }
    
    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('SCOPE_read') and (hasRole('ROLE_ADMIN') or #username == authentication.name)")
    @Operation(
        summary = "Find user by username",
        description = "Retrieves a user by their username. Users can only access their own data unless they have ADMIN role.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
        }
    )
    public ResponseEntity<UserResponse> findByUsername(
            @Parameter(description = "Username", required = true) 
            @PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }
    
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('SCOPE_read') and (hasRole('ROLE_ADMIN') or @userService.isUserWithEmail(#email, authentication.name))")
    @Operation(
        summary = "Find user by email",
        description = "Retrieves a user by their email address. Users can only access their own data unless they have ADMIN role.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
        }
    )
    public ResponseEntity<UserResponse> findByEmail(
            @Parameter(description = "Email address", required = true) 
            @PathVariable String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('SCOPE_read')")
    @Operation(
        summary = "Get current user details",
        description = "Retrieves the details of the currently authenticated user",
        responses = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
        }
    )
    public ResponseEntity<UserResponse> getCurrentUser(JwtAuthenticationToken authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.findByUsername(username));
    }
    
    @GetMapping("/token-info")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get token information",
        description = "Returns information about the current OAuth2 token",
        responses = {
            @ApiResponse(responseCode = "200", description = "Token information retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
        }
    )
    public ResponseEntity<?> getTokenInfo(JwtAuthenticationToken authentication) {
        return ResponseEntity.ok(authentication.getTokenAttributes());
    }
}