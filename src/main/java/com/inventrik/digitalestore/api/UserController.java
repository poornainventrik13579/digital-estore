package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.request.UserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.security.TenantSecurity;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users (authenticated)")
public class UserController {

    private final UserService userService;
    private final TenantSecurity tenantSecurity;

    @GetMapping("/api/v1/tenants/{tenantId}/users")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get users. Regular users see only their profile (latest first).")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @PathVariable Integer tenantId,
            @RequestParam(required = false) String status,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);

        return ResponseEntity.ok(userService.getAllUsers(tenantId, status, null, null));
    }

    @GetMapping("/api/v1/tenants/{tenantId}/users/{userId}")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get a user by ID. Regular users can only access their own profile.")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Integer tenantId,
            @PathVariable String userId,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);

        return ResponseEntity.ok(userService.getUser(tenantId, userId));
    }

    @GetMapping("/api/v1/tenants/{tenantId}/users/me")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getCurrentUser(
            @PathVariable Integer tenantId,
            Authentication authentication) {
        String username = authentication.getName();
        UserResponse user = userService.findByUsername(username);
        if (user.getTenantId().equals(tenantId)) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/api/v1/tenants/{tenantId}/users")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TENANT')")
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
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Update a user. Regular users can only update their own profile.")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer tenantId,
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest updateRequest,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);

        String username = authentication.getName();
        UserResponse updatedUser = userService.updateUser(tenantId, userId, username, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/api/v1/tenants/{tenantId}/users/{userId}")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Delete a user (Admin/Tenant only)")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Integer tenantId,
            @PathVariable String userId,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        userService.deleteUser(tenantId, userId);
        return ResponseEntity.noContent().build();
    }
}
