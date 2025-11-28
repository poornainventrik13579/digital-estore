package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.request.UserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    // Get all users for a tenant with optional filters (status, username, email)
    List<UserResponse> getAllUsers(Integer tenantId, String status, String username, String email);

    // Get a single user by ID
    UserResponse getUser(Integer tenantId, Long userId);

    // Create a new user
    UserResponse createUser(Integer tenantId, String createdBy, UserRequest userRequest);

    // Update an existing user
    UserResponse updateUser(Integer tenantId, Long userId, String updatedBy, UserUpdateRequest updateRequest);

    // Delete a user
    void deleteUser(Integer tenantId, Long userId);

    // Find user by username (for authentication)
    UserResponse findByUsername(String username);

    // Find user by email (for authentication)
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