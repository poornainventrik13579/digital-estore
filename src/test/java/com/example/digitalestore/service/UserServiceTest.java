package com.example.digitalestore.service;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.domain.user.UserRole;
import com.inventrik.digitalestore.domain.user.UserType;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.request.TenantUserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.notification.EmailNotificationService;
import com.inventrik.digitalestore.service.user.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IdGeneratorService idGeneratorService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldGetUsersByTenant() {
        Integer tenantId = 1;
        User user1 = createUser(tenantId, 1L, "user1", "user1@test.com");
        User user2 = createUser(tenantId, 2L, "user2", "user2@test.com");

        when(userRepository.findByTenantId(tenantId)).thenReturn(Arrays.asList(user1, user2));

        List<UserResponse> result = userService.getUsersByTenant(tenantId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("user1");
        assertThat(result.get(1).getUsername()).isEqualTo("user2");
        verify(userRepository).findByTenantId(tenantId);
    }

    @Test
    void shouldGetUserByTenantAndUserId() {
        Integer tenantId = 1;
        Long userId = 1L;
        User user = createUser(tenantId, userId, "testuser", "test@example.com");

        when(userRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserByTenantAndUserId(tenantId, userId);

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByTenantIdAndUserId(tenantId, userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        Integer tenantId = 1;
        Long userId = 1L;

        when(userRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByTenantAndUserId(tenantId, userId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findByTenantIdAndUserId(tenantId, userId);
    }

    @Test
    void shouldCreateUserForTenant() {
        Integer tenantId = 1;
        Long generatedUserId = 123L;
        TenantUserSignupRequest request = new TenantUserSignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setFirstName("New");
        request.setLastName("User");
        request.setPassword("password123");

        User savedUser = createUser(tenantId, generatedUserId, "newuser", "newuser@test.com");

        when(userRepository.existsByTenantIdAndUsername(tenantId, "newuser")).thenReturn(false);
        when(userRepository.existsByTenantIdAndEmail(tenantId, "newuser@test.com")).thenReturn(false);
        when(userRepository.countByTenantId(tenantId)).thenReturn(1L);
        when(idGeneratorService.generateUserId()).thenReturn(generatedUserId);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = userService.createUserForTenant(tenantId, request, "admin");

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("newuser@test.com");
        verify(userRepository).existsByTenantIdAndUsername(tenantId, "newuser");
        verify(userRepository).existsByTenantIdAndEmail(tenantId, "newuser@test.com");
        verify(userRepository).save(any(User.class));
        verify(emailNotificationService, never()).sendWelcomeEmail(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        Integer tenantId = 1;
        TenantUserSignupRequest request = new TenantUserSignupRequest();
        request.setUsername("existinguser");
        request.setEmail("new@test.com");

        User existingUser = createUser(tenantId, 1L, "existinguser", "existing@test.com");

        when(userRepository.findByTenantIdAndUsername(tenantId, "existinguser"))
            .thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.createUserForTenant(tenantId, request, "admin"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Username already exists");

        verify(userRepository).findByTenantIdAndUsername(tenantId, "existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        Integer tenantId = 1;
        TenantUserSignupRequest request = new TenantUserSignupRequest();
        request.setUsername("newuser");
        request.setEmail("existing@test.com");

        User existingUser = createUser(tenantId, 1L, "otheruser", "existing@test.com");

        when(userRepository.findByTenantIdAndUsername(tenantId, "newuser")).thenReturn(Optional.empty());
        when(userRepository.findByTenantIdAndEmail(tenantId, "existing@test.com"))
            .thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.createUserForTenant(tenantId, request, "admin"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Email already exists");

        verify(userRepository).findByTenantIdAndEmail(tenantId, "existing@test.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldUpdateUserInTenant() {
        Integer tenantId = 1;
        Long userId = 1L;
        User existingUser = createUser(tenantId, userId, "testuser", "test@example.com");
        TenantUserUpdateRequest updateRequest = new TenantUserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setPhone("1234567890");

        when(userRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserResponse result = userService.updateUserInTenant(tenantId, userId, updateRequest, "admin");

        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Name");
        verify(userRepository).findByTenantIdAndUserId(tenantId, userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldDeleteUserFromTenant() {
        Integer tenantId = 1;
        Long userId = 1L;
        User existingUser = createUser(tenantId, userId, "testuser", "test@example.com");

        when(userRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userRepository).deleteById(any());

        userService.deleteUserFromTenant(tenantId, userId);

        verify(userRepository).findByTenantIdAndUserId(tenantId, userId);
        verify(userRepository).deleteById(new User.UserPK(tenantId, userId));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        Integer tenantId = 1;
        Long userId = 1L;

        when(userRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserFromTenant(tenantId, userId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findByTenantIdAndUserId(tenantId, userId);
        verify(userRepository, never()).deleteById(any());
    }


    private User createUser(Integer tenantId, Long userId, String username, String email) {
        User user = new User();
        user.setTenantId(tenantId);
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUserType(UserType.INDIVIDUAL);
        user.setUserRole(UserRole.USER);
        user.setPasswordHash("encodedPassword");
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        return user;
    }
}