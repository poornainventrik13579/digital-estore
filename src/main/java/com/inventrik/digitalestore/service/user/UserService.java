package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.request.TenantUserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    
    List<UserResponse> getUsersByTenant(Integer tenantId);
    
    UserResponse getUserByTenantAndUserId(Integer tenantId, Long userId);
    
    UserResponse createUserForTenant(Integer tenantId, TenantUserSignupRequest userRequest, String createdBy);
    
    UserResponse updateUserInTenant(Integer tenantId, Long userId, TenantUserUpdateRequest updateRequest, String updatedBy);
    
    void deleteUserFromTenant(Integer tenantId, Long userId);
    
    List<UserResponse> getActiveUsersByTenant(Integer tenantId);
    
    List<UserResponse> getTenantAdmins(Integer tenantId);
    
    List<UserResponse> getAllUsers(Integer tenantId);
    
    UserResponse getUser(Integer tenantId, Long userId);
    
    UserResponse createUser(Integer tenantId, String createdBy, TenantUserSignupRequest userRequest);
    
    UserResponse updateUser(Integer tenantId, Long userId, String updatedBy, TenantUserUpdateRequest updateRequest);
    
    void deleteUser(Integer tenantId, Long userId);
    
    List<UserResponse> getActiveUsers(Integer tenantId);
    
    UserResponse findByUsername(String username);
    
    UserResponse mapToUserResponse(User user);
    
    UserResponse findByEmail(String email);
    boolean isCurrentUser(Integer tenantId, Long userId, String username);
    boolean isUserWithEmail(String email, String username);
    
    void sendPasswordResetEmail(String email);
    
    String getAuditCode(String username);
    
    String truncateUsernameForAudit(String username);

}