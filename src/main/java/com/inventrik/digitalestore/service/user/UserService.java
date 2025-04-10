package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.request.UserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    
    // Get all users for a tenant
    List<UserResponse> getAllUsers(Integer tenantId);
    
    // Get a single user by ID
    UserResponse getUser(Integer tenantId, Long userId);
    
    // Create a new user
    UserResponse createUser(Integer tenantId, String createdBy, UserRequest userRequest);
    
    // Update an existing user
    UserResponse updateUser(Integer tenantId, Long userId, String updatedBy, UserUpdateRequest updateRequest);
    
    // Delete a user
    void deleteUser(Integer tenantId, Long userId);
    
    // Get active users
    List<UserResponse> getActiveUsers(Integer tenantId);
    
    // Find user by username
    UserResponse findByUsername(String username);
    
    // Find user by email
    UserResponse findByEmail(String email);
}