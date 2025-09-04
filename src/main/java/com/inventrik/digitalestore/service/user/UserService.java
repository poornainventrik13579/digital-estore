package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.request.TenantUserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    
    // ================================
    // TENANT-SCOPED USER METHODS
    // ================================
    
    // Get all users for a tenant
    List<UserResponse> getUsersByTenant(Integer tenantId);
    
    // Get user by tenant and user ID
    UserResponse getUserByTenantAndUserId(Integer tenantId, Long userId);
    
    // Create a new user for a tenant
    UserResponse createUserForTenant(Integer tenantId, TenantUserSignupRequest userRequest, String createdBy);
    
    // Update user in tenant
    UserResponse updateUserInTenant(Integer tenantId, Long userId, TenantUserUpdateRequest updateRequest, String updatedBy);
    
    // Delete user from tenant
    void deleteUserFromTenant(Integer tenantId, Long userId);
    
    // Get active users in tenant
    List<UserResponse> getActiveUsersByTenant(Integer tenantId);
    
    // Get tenant admins
    List<UserResponse> getTenantAdmins(Integer tenantId);
    
    // Promote user to admin
    UserResponse promoteUserToAdmin(Integer tenantId, Long userId, String updatedBy);
    
    // Demote admin to user
    UserResponse demoteAdminToUser(Integer tenantId, Long userId, String updatedBy);
    
    // ================================
    // LEGACY METHODS (DEPRECATED)
    // ================================
    
    // Get all users for a tenant
    List<UserResponse> getAllUsers(Integer tenantId);
    
    // Get a single user by ID
    UserResponse getUser(Integer tenantId, Long userId);
    
    // Create a new user
    UserResponse createUser(Integer tenantId, String createdBy, TenantUserSignupRequest userRequest);
    
    // Update an existing user
    UserResponse updateUser(Integer tenantId, Long userId, String updatedBy, TenantUserUpdateRequest updateRequest);
    
    // Delete a user
    void deleteUser(Integer tenantId, Long userId);
    
    // Get active users
    List<UserResponse> getActiveUsers(Integer tenantId);
    
    // Find user by username
    UserResponse findByUsername(String username);
    
    // ================================
    // UTILITY METHODS
    // ================================
    
    // Map User entity to UserResponse DTO
    UserResponse mapToUserResponse(User user);
    
    // Find user by email
    UserResponse findByEmail(String email);
    boolean isCurrentUser(Integer tenantId, Long userId, String username);
    boolean isUserWithEmail(String email, String username);
    
    // Forgot password functionality
    void sendPasswordResetEmail(String email);
    
    // Get audit code for username
    String getAuditCode(String username);
    
    // Safely truncate username for audit fields
    String truncateUsernameForAudit(String username);

}