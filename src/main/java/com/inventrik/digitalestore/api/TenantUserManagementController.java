package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.user.UserRole;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.request.TenantUserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.security.TenantAccessValidator;
import com.inventrik.digitalestore.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/users")
@RequiredArgsConstructor
@Tag(name = "Tenant User Management", description = "Inventrik tenant-scoped user management APIs")
@SecurityRequirement(name = "oauth2")
@Slf4j
public class TenantUserManagementController {
    
    private final UserService userService;
    private final TenantAccessValidator tenantAccessValidator;
    
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Get all users in tenant", 
               description = "Get all users in the authenticated user's tenant (Admin only)")
    public ResponseEntity<List<UserResponse>> getTenantUsers(
            @PathVariable Integer tenantId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        log.info("Get all users request for tenant: {}", tenantId);
        
        List<UserResponse> users = userService.getUsersByTenant(tenantId);
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get user details", 
               description = "Get user details within tenant (user can access own data, admins can access any user)")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Integer tenantId,
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isAdmin = tenantAccessValidator.isTenantAdmin(authentication, tenantId);
        String username = authentication.getName();

        log.info("Get user details request for user: {} in tenant: {}", userId, tenantId);

        UserResponse user = userService.getUserByTenantAndUserId(tenantId, userId);

        if (!isAdmin && !userService.isCurrentUser(tenantId, userId, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Add user to tenant", 
               description = "Add new user to tenant (Tenant Admin only)")
    public ResponseEntity<UserResponse> createUser(
            @PathVariable Integer tenantId,
            @Valid @RequestBody TenantUserSignupRequest userRequest,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String createdBy = authentication.getName();
        
        log.info("Create user request for tenant: {} by: {}", tenantId, createdBy);
        
        UserResponse createdUser = userService.createUserForTenant(tenantId, userRequest, createdBy);
        
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update user", 
               description = "Update user details (user can update own data, admins can update any user)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer tenantId,
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Valid @RequestBody TenantUserUpdateRequest updateRequest,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isAdmin = tenantAccessValidator.isTenantAdmin(authentication, tenantId);
        String updatedBy = authentication.getName();

        if (!isAdmin && !userService.isCurrentUser(tenantId, userId, updatedBy)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Update user request for user: {} in tenant: {} by: {}", userId, tenantId, updatedBy);

        UserResponse updatedUser = userService.updateUserInTenant(tenantId, userId, updateRequest, updatedBy);

        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Delete user from tenant",
               description = "Delete user from tenant (Tenant Admin only, cannot delete self)")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Integer tenantId,
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            Authentication authentication) {

        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Delete user request for user: {} in tenant: {}", userId, tenantId);

        String username = authentication.getName();

        userService.deleteUserFromTenant(tenantId, userId, username);

        return ResponseEntity.noContent().build();
    }
}
