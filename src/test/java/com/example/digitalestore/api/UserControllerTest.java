package com.example.digitalestore.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventrik.digitalestore.api.TenantUserManagementController;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.request.TenantUserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TenantUserManagementController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldGetAllUsers() throws Exception {
        UserResponse user1 = createUserResponse(1L, "user1", "user1@test.com");
        UserResponse user2 = createUserResponse(2L, "user2", "user2@test.com");
        List<UserResponse> users = Arrays.asList(user1, user2);

        when(userService.getUsersByTenant(1)).thenReturn(users);

        mockMvc.perform(get("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));

        verify(userService).getUsersByTenant(1);
    }

    @Test
    @WithMockUser
    void shouldGetUserById() throws Exception {
        UserResponse user = createUserResponse(1L, "testuser", "test@example.com");

        when(userService.getUserByTenantAndUserId(1, 1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/1")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserByTenantAndUserId(1, 1L);
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(userService.getUserByTenantAndUserId(1, 999L))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/999")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).getUserByTenantAndUserId(1, 999L);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldCreateUser() throws Exception {
        TenantUserSignupRequest request = new TenantUserSignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setFirstName("New");
        request.setLastName("User");
        request.setPassword("password123");

        UserResponse createdUser = createUserResponse(1L, "newuser", "newuser@test.com");

        when(userService.createUserForTenant(eq(1), any(TenantUserSignupRequest.class), anyString()))
                .thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1).claim("sub", "admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@test.com"));

        verify(userService).createUserForTenant(eq(1), any(TenantUserSignupRequest.class), eq("admin"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldReturnBadRequestWhenCreatingUserWithInvalidData() throws Exception {
        TenantUserSignupRequest request = new TenantUserSignupRequest();

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUserForTenant(anyInt(), any(), anyString());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldReturnConflictWhenUsernameExists() throws Exception {
        TenantUserSignupRequest request = new TenantUserSignupRequest();
        request.setUsername("existinguser");
        request.setEmail("new@test.com");
        request.setFirstName("New");
        request.setLastName("User");
        request.setPassword("password123");

        when(userService.createUserForTenant(eq(1), any(TenantUserSignupRequest.class), anyString()))
                .thenThrow(new BusinessException("Username already exists"));

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1).claim("sub", "admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService).createUserForTenant(eq(1), any(TenantUserSignupRequest.class), eq("admin"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldUpdateUser() throws Exception {
        TenantUserUpdateRequest updateRequest = new TenantUserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setPhone("1234567890");

        UserResponse updatedUser = createUserResponse(1L, "testuser", "test@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");

        when(userService.updateUserInTenant(eq(1), eq(1L), any(TenantUserUpdateRequest.class), anyString()))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/users/1")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1).claim("sub", "admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));

        verify(userService).updateUserInTenant(eq(1), eq(1L), any(TenantUserUpdateRequest.class), eq("admin"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUserFromTenant(1, 1L);

        mockMvc.perform(delete("/api/v1/users/1")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1))))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserFromTenant(1, 1L);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldReturnNotFoundWhenDeletingNonExistentUser() throws Exception {
        doThrow(new ResourceNotFoundException("User not found"))
                .when(userService).deleteUserFromTenant(1, 999L);

        mockMvc.perform(delete("/api/v1/users/999")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1))))
                .andExpect(status().isNotFound());

        verify(userService).deleteUserFromTenant(1, 999L);
    }

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUsersByTenant(anyInt());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void shouldReturnForbiddenWhenUserTriesToCreateUser() throws Exception {
        TenantUserSignupRequest request = new TenantUserSignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).createUserForTenant(anyInt(), any(), anyString());
    }

    @Test
    @WithMockUser
    void shouldHandleMultipleTenants() throws Exception {
        UserResponse user = createUserResponse(1L, "testuser", "test@example.com");

        when(userService.getUserByTenantAndUserId(2, 1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/1")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 2)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).getUserByTenantAndUserId(2, 1L);
    }

    private UserResponse createUserResponse(Long userId, String username, String email) {
        UserResponse response = new UserResponse();
        response.setUserId(userId);
        response.setUsername(username);
        response.setEmail(email);
        response.setFirstName("Test");
        response.setLastName("User");
        return response;
    }
}