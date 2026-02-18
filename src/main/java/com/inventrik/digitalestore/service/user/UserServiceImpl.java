package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.domain.user.UserRole;
import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.request.UserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.notification.EmailNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
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

    /**
     * Validates password complexity requirements
     * Password must be at least 8 characters with uppercase, lowercase, and number
     */
    private void validatePasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BusinessException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BusinessException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BusinessException("Password must contain at least one number");
        }
    }

    @Override
    public List<UserResponse> getAllUsers(Integer tenantId, String status, String username, String email) {
        if (username != null && !username.trim().isEmpty()) {
            return userRepository.findByTenantIdAndUsername(tenantId, username).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        if (email != null && !email.trim().isEmpty()) {
            return userRepository.findByTenantIdAndEmail(tenantId, email).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            String statusCode = "ACTIVE".equalsIgnoreCase(status) ? "0" : "1";
            return userRepository.findByTenantIdAndStatus(tenantId, statusCode).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        return userRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserResponse getUser(Integer tenantId, String userId) {
        User user = userRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToDTO(user);
    }

    @Override
    public boolean isCurrentUser(Integer tenantId, String userId, String username) {
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
    public UserResponse createUser(Integer tenantId, String createdBy, UserRequest userRequest) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        // Validate password complexity before proceeding
        validatePasswordComplexity(userRequest.getPassword());

        if (userRepository.existsByTenantIdAndUsername(tenantId, userRequest.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByTenantIdAndEmail(tenantId, userRequest.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        if (userRequest.getPhone() != null && userRepository.existsByTenantIdAndPhone(tenantId, userRequest.getPhone())) {
            throw new BusinessException("Phone number already exists");
        }

        String newUserId = idGeneratorService.generateId(tenantId, "USER");
        // TODO: OTP feature commented out - enable when email verification is ready
        // String otp = generateOTP();

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
        user.setUserRole(userRequest.getUserRole() != null ? userRequest.getUserRole() : UserRole.USER);

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

        // TODO: OTP feature commented out
        // user.setOtp(otp);
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setStatus("0"); // Active status
        // Use proper audit code from createdBy parameter
        user.setCreatedBy(getAuditCode(createdBy));
        user.setUpdatedBy(getAuditCode(createdBy));
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        emailNotificationService.sendWelcomeEmail(savedUser);

        return mapToDTO(savedUser);
    }
    
    @Override
    @Transactional
    public UserResponse updateUser(Integer tenantId, String userId, String updatedBy, UserUpdateRequest updateRequest) {
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

        if (updateRequest.getUserRole() != null) {
             user.setUserRole(updateRequest.getUserRole());
        }
        
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
        if (updateRequest.getStatus() != null) {
            user.setStatus(updateRequest.getStatus());
        }
        
        // Ensure updatedBy doesn't exceed 2 characters
        user.setUpdatedBy(getAuditCode(updatedBy));
        user.setUpdated(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        
        return mapToDTO(updatedUser);
    }
    
    @Override
    @Transactional
    public void deleteUser(Integer tenantId, String userId) {
        if (!userRepository.findByTenantIdAndUserId(tenantId, userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        userRepository.deleteByTenantIdAndUserId(tenantId, userId);
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

            // TODO: OTP/Reset token feature commented out
            // Generate reset token and store in OTP field
            // String resetToken = generateOTP();
            // user.setOtp(resetToken);
            // user.setUpdated(LocalDateTime.now());
            // userRepository.save(user);

            // Send password reset email (without token for now)
            String resetToken = generateOTP();
            emailNotificationService.sendPasswordResetEmail(user, resetToken);

        } catch (ResourceNotFoundException e) {
            // For security, don't reveal if email exists
            log.warn("Password reset attempted for non-existent email: {}", email);
        }
    }

    // TODO: OTP features - implement when email verification is ready
    /*
    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Invalid email or OTP"));

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new BusinessException("Invalid or expired OTP");
        }

        validatePasswordComplexity(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setOtp(null); // Clear OTP after use
        user.setUpdated(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Invalid email or OTP"));

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new BusinessException("Invalid or expired OTP");
        }

        user.setOtp(null); // Clear OTP after verification
        user.setStatus("0"); // Activate user
        user.setUpdated(LocalDateTime.now());
        userRepository.save(user);
    }
    */
    
    public String getAuditCode(String username) {
                if (username == null || username.isEmpty()) {
            return "SY";  // System
        }
        try {
            UserResponse user = findByUsername(username);
            
            // Return code based on user role
            if (user.getUserRole() != null) {
                switch (user.getUserRole()) {
                    case ADMIN:
                        return "AD";  // Admin
                    case TENANT:
                        return "TN";  // Tenant
                    case USER:
                        return "US";  // User
                    default:
                        return "US";  // Default to User
                }
            }
            return "US";  // Default if role is null
            
        } catch (Exception e) {
            // Fallback if user not found
            log.warn("User not found for audit code generation: {}", username);
            return "UN";  // Unknown
        }
    }
    
    /**
     * Safely truncates username to 2 characters for database audit fields
     */
    public String truncateUsernameForAudit(String username) {
        return getAuditCode(username);
    }
}