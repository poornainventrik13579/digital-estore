package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.request.TenantUserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.notification.EmailNotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService emailNotificationService;
    private final IdGeneratorService idGeneratorService;

    // Utility method to convert Entity to DTO
    private UserResponse mapToDTO(User user) {
        return new UserResponse(
            user.getUserId(),
            user.getTenantId(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getImage(),
            user.getPhone(),
            user.getEmail(),
            user.getUserType(),
            user.getUserRole(),
            user.getCompanyName(),
            user.getCompanyRegistrationNumber(),
            user.getCompanyAddress1(),
            user.getCompanyAddress2(),
            user.getCompanyCountry(),
            user.getCompanyPincode(),
            user.getTaxId(),
            user.getStatus(),
            user.getCreated(),
            user.getUpdated()
        );
    }
    
    @Override
    public List<UserResponse> getAllUsers(Integer tenantId) {
        return userRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserResponse getUser(Integer tenantId, Long userId) {
        User user = userRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToDTO(user);
    }
    
    @Override
    public boolean isCurrentUser(Integer tenantId, Long userId, String username) {
        try {
            User user = userRepository.findByTenantIdAndUserId(tenantId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            return user.getUsername().equals(username);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isUserWithEmail(String email, String username) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
            return user.getUsername().equals(username);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    @Transactional
    public UserResponse createUser(Integer tenantId, String createdBy, TenantUserSignupRequest userRequest) {
        // Check for duplicate username, email, phone
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        if (userRepository.existsByPhone(userRequest.getPhone())) {
            throw new BusinessException("Phone number already exists");
        }
        
        // Generate a new user ID
        Long newUserId = idGeneratorService.generateId(tenantId, "USER");
        
        // Generate OTP
        String otp = generateOTP();
        
        User user = new User();
        user.setTenantId(tenantId);
        user.setUserId(newUserId);
        user.setUsername(userRequest.getUsername());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setImage(userRequest.getImage());
        user.setPhone(userRequest.getPhone());
        user.setEmail(userRequest.getEmail());
        user.setUserType(userRequest.getUserType());
        user.setUserRole(com.inventrik.digitalestore.domain.user.UserRole.USER); // Default role for new users
        
        // Set company details if user type is COMPANY
        if (userRequest.getUserType() != null && userRequest.getUserType() == com.inventrik.digitalestore.domain.user.UserType.COMPANY) {
            user.setCompanyName(userRequest.getCompanyName());
            user.setCompanyRegistrationNumber(userRequest.getCompanyRegistrationNumber());
            user.setCompanyAddress1(userRequest.getCompanyAddress1());
            user.setCompanyAddress2(userRequest.getCompanyAddress2());
            user.setCompanyCountry(userRequest.getCompanyCountry());
            user.setCompanyPincode(userRequest.getCompanyPincode());
            user.setTaxId(userRequest.getTaxId());
        }
        
        user.setOtp(otp);
        // Encode password
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setStatus("0"); // Active status
        // Ensure createdBy doesn't exceed 2 characters
        user.setCreatedBy(String.format("%02d", newUserId % 98 + 1));
        user.setUpdatedBy(String.format("%02d", newUserId % 98 + 1));
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        emailNotificationService.sendWelcomeEmail(savedUser);
        
        return mapToDTO(savedUser);
    }
    
    @Override
    @Transactional
    public UserResponse updateUser(Integer tenantId, Long userId, String updatedBy, TenantUserUpdateRequest updateRequest) {
        User user = userRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Update user properties if provided
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getImage() != null) {
            user.setImage(updateRequest.getImage());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getUserType() != null) {
            user.setUserType(updateRequest.getUserType());
        }
        // Note: User role changes should be handled by separate admin methods (promoteUserToAdmin, demoteUserFromAdmin)
        
        // Update company details
        if (updateRequest.getCompanyName() != null) {
            user.setCompanyName(updateRequest.getCompanyName());
        }
        if (updateRequest.getCompanyRegistrationNumber() != null) {
            user.setCompanyRegistrationNumber(updateRequest.getCompanyRegistrationNumber());
        }
        if (updateRequest.getCompanyAddress1() != null) {
            user.setCompanyAddress1(updateRequest.getCompanyAddress1());
        }
        if (updateRequest.getCompanyAddress2() != null) {
            user.setCompanyAddress2(updateRequest.getCompanyAddress2());
        }
        if (updateRequest.getCompanyCountry() != null) {
            user.setCompanyCountry(updateRequest.getCompanyCountry());
        }
        if (updateRequest.getCompanyPincode() != null) {
            user.setCompanyPincode(updateRequest.getCompanyPincode());
        }
        if (updateRequest.getTaxId() != null) {
            user.setTaxId(updateRequest.getTaxId());
        }
        // Note: User status changes should be handled by separate admin methods (activateUser, deactivateUser)
        
        // Ensure updatedBy doesn't exceed 2 characters
        user.setUpdatedBy(getAuditCode(updatedBy));
        user.setUpdated(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        
        return mapToDTO(updatedUser);
    }
    
    @Override
    @Transactional
    public void deleteUser(Integer tenantId, Long userId) {
        if (!userRepository.findByTenantIdAndUserId(tenantId, userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        userRepository.deleteByTenantIdAndUserId(tenantId, userId);
    }
    
    @Override
    public List<UserResponse> getActiveUsers(Integer tenantId) {
        return userRepository.findByTenantIdAndStatus(tenantId, "0").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToDTO(user);
    }
    
    @Override
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToDTO(user);
    }
    
    // Helper method to generate OTP
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }
    
    @Override
    public void sendPasswordResetEmail(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
            
            // Generate reset token (using OTP for simplicity)
            String resetToken = generateOTP();
            
            // Send password reset email
            emailNotificationService.sendPasswordResetEmail(user, resetToken);
            
        } catch (ResourceNotFoundException e) {
            // For security reasons, don't reveal if email exists or not
            // Just log the error and return success to user
            System.out.println("Password reset attempted for non-existent email: " + email);
        }
    }
    
    public String getAuditCode(String username) {
        if (username == null || username.equals("system")) {
            return "00";
        }
        if (username.equals("webhook")) {
            return "99";
        }
        
        try {
            UserResponse user = findByUsername(username);
            return String.format("%02d", user.getUserId() % 98 + 1);
        } catch (Exception e) {
            int hash = Math.abs(username.hashCode()) % 89 + 10;
            return String.format("%02d", hash);
        }
    }
    
    /**
     * Safely truncates username to 2 characters for database audit fields
     */
    public String truncateUsernameForAudit(String username) {
        if (username == null || username.isEmpty()) {
            return "00";
        }
        return username.length() > 2 ? username.substring(0, 2) : username;
    }
    
    // ================================
    // NEW TENANT-SCOPED METHODS
    // ================================
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByTenant(Integer tenantId) {
        return userRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByTenantAndUserId(Integer tenantId, Long userId) {
        User user = userRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserResponse(user);
    }
    
    @Override
    public UserResponse createUserForTenant(Integer tenantId, TenantUserSignupRequest userRequest, String createdBy) {
        // Implementation would go here - delegate to existing createUser for now
        return createUser(tenantId, createdBy, userRequest);
    }
    
    @Override
    public UserResponse updateUserInTenant(Integer tenantId, Long userId, TenantUserUpdateRequest updateRequest, String updatedBy) {
        // Implementation would go here - delegate to existing updateUser for now
        return updateUser(tenantId, userId, updatedBy, updateRequest);
    }
    
    @Override
    public void deleteUserFromTenant(Integer tenantId, Long userId) {
        // Implementation would go here - delegate to existing deleteUser for now
        deleteUser(tenantId, userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsersByTenant(Integer tenantId) {
        return userRepository.findByTenantIdAndStatus(tenantId, "A")
            .stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getTenantAdmins(Integer tenantId) {
        return userRepository.findByTenantIdAndUserRole(tenantId, com.inventrik.digitalestore.domain.user.UserRole.TENANT_ADMIN)
            .stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public UserResponse promoteUserToAdmin(Integer tenantId, Long userId, String updatedBy) {
        User user = userRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setUserRole(com.inventrik.digitalestore.domain.user.UserRole.TENANT_ADMIN);
        user.setUpdatedBy(updatedBy);
        user = userRepository.save(user);
        
        return mapToUserResponse(user);
    }
    
    @Override
    public UserResponse demoteAdminToUser(Integer tenantId, Long userId, String updatedBy) {
        User user = userRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setUserRole(com.inventrik.digitalestore.domain.user.UserRole.USER);
        user.setUpdatedBy(updatedBy);
        user = userRepository.save(user);
        
        return mapToUserResponse(user);
    }
    
    @Override
    public UserResponse mapToUserResponse(User user) {
        return mapToDTO(user); // Delegate to existing mapToDTO method
    }
}