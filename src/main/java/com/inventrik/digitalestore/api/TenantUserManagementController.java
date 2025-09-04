package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.user.UserRole;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.request.TenantUserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Tenant User Management", description = "Inventrik tenant-scoped user management APIs")
@SecurityRequirement(name = "oauth2")
@Slf4j
public class TenantUserManagementController {
    
    private final UserService userService;
    
    /**
     * Extract tenant ID from JWT token
     */
    private Integer extractTenantIdFromJwt(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return null;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("tenant_id");
    }
    
    /**
     * Extract user ID from JWT token
     */
    private Long extractUserIdFromJwt(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return null;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("user_id");
    }
    
    /**
     * Check if current user has admin privileges in their tenant
     */
    private boolean hasAdminPrivileges(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return false;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userRole = jwt.getClaim("user_role");
        return UserRole.TENANT_ADMIN.name().equals(userRole) || UserRole.SYSTEM_ADMIN.name().equals(userRole);
    }
    
    @GetMapping
    @Operation(summary = "Get all users in tenant", 
               description = "Get all users in the authenticated user's tenant (Admin only)")
    public ResponseEntity<List<UserResponse>> getTenantUsers(Authentication authentication) {
        
        Integer tenantId = extractTenantIdFromJwt(authentication);
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (!hasAdminPrivileges(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        log.info("Get all users request for tenant: {}", tenantId);
        
        List<UserResponse> users = userService.getUsersByTenant(tenantId);
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user details", 
               description = "Get user details within tenant (user can access own data, admins can access any user)")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            Authentication authentication) {
        
        Integer tenantId = extractTenantIdFromJwt(authentication);
        Long currentUserId = extractUserIdFromJwt(authentication);
        
        if (tenantId == null || currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // User can access their own data, or admin can access any user in their tenant
        if (!userId.equals(currentUserId) && !hasAdminPrivileges(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        log.info("Get user details request for user: {} in tenant: {}", userId, tenantId);
        
        UserResponse user = userService.getUserByTenantAndUserId(tenantId, userId);
        
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    @Operation(summary = "Add user to tenant", 
               description = "Add new user to tenant (Tenant Admin only)")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody TenantUserSignupRequest userRequest,
            Authentication authentication) {
        
        Integer tenantId = extractTenantIdFromJwt(authentication);
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (!hasAdminPrivileges(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String createdBy = authentication.getName();
        
        log.info("Create user request for tenant: {} by: {}", tenantId, createdBy);
        
        UserResponse createdUser = userService.createUserForTenant(tenantId, userRequest, createdBy);
        
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    
    @PutMapping("/{userId}")
    @Operation(summary = "Update user", 
               description = "Update user details (user can update own data, admins can update any user)")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Valid @RequestBody TenantUserUpdateRequest updateRequest,
            Authentication authentication) {
        
        Integer tenantId = extractTenantIdFromJwt(authentication);
        Long currentUserId = extractUserIdFromJwt(authentication);
        
        if (tenantId == null || currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // User can update their own data, or admin can update any user in their tenant
        if (!userId.equals(currentUserId) && !hasAdminPrivileges(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String updatedBy = authentication.getName();
        
        log.info("Update user request for user: {} in tenant: {} by: {}", userId, tenantId, updatedBy);
        
        UserResponse updatedUser = userService.updateUserInTenant(tenantId, userId, updateRequest, updatedBy);
        
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user from tenant", 
               description = "Delete user from tenant (Tenant Admin only, cannot delete self)")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            Authentication authentication) {
        
        Integer tenantId = extractTenantIdFromJwt(authentication);
        Long currentUserId = extractUserIdFromJwt(authentication);
        
        if (tenantId == null || currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (!hasAdminPrivileges(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Prevent admin from deleting themselves
        if (userId.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        log.info("Delete user request for user: {} in tenant: {}", userId, tenantId);
        
        userService.deleteUserFromTenant(tenantId, userId);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active users in tenant", 
               description = "Get all active users in tenant (Admin only)")
    public ResponseEntity<List<UserResponse>> getActiveTenantUsers(Authentication authentication) {
        
        Integer tenantId = extractTenantIdFromJwt(authentication);
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (!hasAdminPrivileges(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        log.info("Get active users request for tenant: {}", tenantId);
        
        List<UserResponse> users = userService.getActiveUsersByTenant(tenantId);
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/admins")
    @Operation(summary = "Get tenant admins", 
               description = "Get all tenant admins (Admin only)")
    public ResponseEntity<List<UserResponse>> getTenantAdmins(Authentication authentication) {
        
        Integer tenantId = extractTenantIdFromJwt(authentication);
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (!hasAdminPrivileges(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        log.info("Get tenant admins request for tenant: {}", tenantId);
        
        List<UserResponse> admins = userService.getTenantAdmins(tenantId);
        
        return ResponseEntity.ok(admins);
    }
    
    @PatchMapping("/{userId}/promote")
    @Operation(summary = "Promote user to tenant admin", 
               description = "Promote user to tenant admin role (Tenant Admin only)")
    public ResponseEntity<UserResponse> promoteToAdmin(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            Authentication authentication) {
        
        Integer tenantId = extractTenantIdFromJwt(authentication);
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (!hasAdminPrivileges(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String updatedBy = authentication.getName();
        
        log.info("Promote user to admin request for user: {} in tenant: {}", userId, tenantId);
        
        UserResponse promotedUser = userService.promoteUserToAdmin(tenantId, userId, updatedBy);
        
        return ResponseEntity.ok(promotedUser);
    }
    
    @PatchMapping("/{userId}/demote")
    @Operation(summary = "Demote admin to regular user", 
               description = "Demote tenant admin to regular user (Tenant Admin only)")
    public ResponseEntity<UserResponse> demoteFromAdmin(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            Authentication authentication) {
        
        Integer tenantId = extractTenantIdFromJwt(authentication);
        Long currentUserId = extractUserIdFromJwt(authentication);
        
        if (tenantId == null || currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (!hasAdminPrivileges(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Prevent admin from demoting themselves
        if (userId.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        String updatedBy = authentication.getName();
        
        log.info("Demote admin to user request for user: {} in tenant: {}", userId, tenantId);
        
        UserResponse demotedUser = userService.demoteAdminToUser(tenantId, userId, updatedBy);
        
        return ResponseEntity.ok(demotedUser);
    }
}
