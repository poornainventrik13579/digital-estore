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
            if ("ALL".equalsIgnoreCase(status)) {
                return userRepository.findByTenantId(tenantId).stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());
            }
            String statusCode = "ACTIVE".equalsIgnoreCase(status) ? "0" : "-1";
            return userRepository.findByTenantIdAndStatus(tenantId, statusCode).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        // Default: return only active users
        return userRepository.findByTenantIdAndStatus(tenantId, "0").stream()
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
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean isUserWithEmail(String email, String username) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
            return user.getUsername().equals(username);
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
    
    @Override
    @Transactional
    public UserResponse createUser(Integer tenantId, String createdBy, UserRequest userRequest) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

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

        User user = new User();
        user.setTenantId(tenantId);
        user.setUserId(newUserId);
        user.setUsername(userRequest.getUsername().toLowerCase());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setImage(userRequest.getImage());
        user.setPhone(userRequest.getPhone());
        user.setEmail(userRequest.getEmail());
        user.setUserType(userRequest.getUserType());

        UserRole assignedRole = UserRole.USER;
        if (userRequest.getUserRole() != null && userRequest.getUserRole() != UserRole.USER) {
            try {
                UserResponse creator = findByUsername(createdBy);
                if (creator.getUserRole() == UserRole.ADMIN || creator.getUserRole() == UserRole.TENANT) {
                    assignedRole = userRequest.getUserRole();
                }
            } catch (ResourceNotFoundException e) {
                log.warn("Creator '{}' not found, defaulting role to USER", createdBy);
            }
        }
        user.setUserRole(assignedRole);

        if (userRequest.getUserType() != null && userRequest.getUserType() == com.inventrik.digitalestore.domain.user.UserType.COMPANY) {
            user.setCompanyName(userRequest.getCompanyName());
            user.setCompanyRegistrationNumber(userRequest.getCompanyRegistrationNumber());
            user.setCompanyAddress1(userRequest.getCompanyAddress1());
            user.setCompanyAddress2(userRequest.getCompanyAddress2());
            user.setCompanyCountry(userRequest.getCompanyCountry());
            user.setCompanyPincode(userRequest.getCompanyPincode());
            user.setTaxId(userRequest.getTaxId());
        }

        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setStatus("0");
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
        
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByTenantIdAndEmail(tenantId, updateRequest.getEmail())) {
                throw new BusinessException("Email already exists");
            }
        }
        if (updateRequest.getPhone() != null && !updateRequest.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByTenantIdAndPhone(tenantId, updateRequest.getPhone())) {
                throw new BusinessException("Phone number already exists");
            }
        }

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
        
        user.setUpdatedBy(getAuditCode(updatedBy));
        user.setUpdated(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        
        return mapToDTO(updatedUser);
    }
    
    @Override
    @Transactional
    public void deleteUser(Integer tenantId, String userId) {
        User user = userRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setStatus("-1");
        user.setUpdated(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToDTO(user);
    }

    @Override
    public UserResponse findByTenantIdAndUsername(Integer tenantId, String username) {
        User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user);
    }

    @Override
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToDTO(user);
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

            // Don't send reset emails to deactivated users
            if (!"0".equals(user.getStatus())) {
                log.warn("Password reset attempted for inactive user: {}", email);
                return;
            }

            // Send password reset email (without token for now)
            String resetToken = generateOTP();
            emailNotificationService.sendPasswordResetEmail(user, resetToken);

        } catch (ResourceNotFoundException e) {
            // For security, don't reveal if email exists
            log.warn("Password reset attempted for non-existent email: {}", email);
        }
    }

    public String getAuditCode(String username) {
                if (username == null || username.isEmpty()) {
            return "SY";
        }
        try {
            UserResponse user = findByUsername(username);
            
            // Return code based on user role
            if (user.getUserRole() != null) {
                switch (user.getUserRole()) {
                    case ADMIN:
                        return "AD";
                    case TENANT:
                        return "TN";
                    case USER:
                        return "US";
                    default:
                        return "US";
                }
            }
            return "US";
            
        } catch (Exception e) {
            // Fallback if user not found
            log.warn("User not found for audit code generation: {}", username);
            return "UN";
        }
    }
    
    public String truncateUsernameForAudit(String username) {
        return getAuditCode(username);
    }
}