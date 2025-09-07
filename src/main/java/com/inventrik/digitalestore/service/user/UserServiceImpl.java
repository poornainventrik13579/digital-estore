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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
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
        
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        if (userRepository.existsByPhone(userRequest.getPhone())) {
            throw new BusinessException("Phone number already exists");
        }
        
        Long newUserId = idGeneratorService.generateId(tenantId, "USER");
        
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
        user.setUserRole(com.inventrik.digitalestore.domain.user.UserRole.USER); 
        
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
        
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setStatus("0"); 
        
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
            
            String resetToken = generateOTP();
            
            emailNotificationService.sendPasswordResetEmail(user, resetToken);
            
        } catch (ResourceNotFoundException e) {
            
            log.info("Password reset attempted for non-existent email: {}", email);
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
    
    public String truncateUsernameForAudit(String username) {
        if (username == null || username.isEmpty()) {
            return "00";
        }
        return getAuditCode(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByTenant(Integer tenantId) {
        return userRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByTenantAndUserId(Integer tenantId, Long userId) {
        User user = userRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user);
    }
    
    @Override
    @Transactional
    public UserResponse createUserForTenant(Integer tenantId, TenantUserSignupRequest userRequest, String createdBy) {
        
        if (userRepository.existsByTenantIdAndUsername(tenantId, userRequest.getUsername())) {
            throw new BusinessException("Username already exists in this store");
        }
        
        if (userRepository.existsByTenantIdAndEmail(tenantId, userRequest.getEmail())) {
            throw new BusinessException("Email already exists in this store");
        }
        
        if (userRequest.getPhone() != null && 
            userRepository.existsByTenantIdAndPhone(tenantId, userRequest.getPhone())) {
            throw new BusinessException("Phone number already exists in this store");
        }
        
        User user = new User();
        user.setTenantId(tenantId);
        user.setUserId(idGeneratorService.generateUserId());
        user.setUsername(userRequest.getUsername());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setImage(userRequest.getImage());
        user.setUserType(userRequest.getUserType());
        
        long userCount = userRepository.countByTenantId(tenantId);
        user.setUserRole(userCount == 0 ? com.inventrik.digitalestore.domain.user.UserRole.TENANT_ADMIN : com.inventrik.digitalestore.domain.user.UserRole.USER);
        
        if (userRequest.getUserType() != null && "BUSINESS".equals(userRequest.getUserType().name())) {
            user.setCompanyName(userRequest.getCompanyName());
            user.setCompanyRegistrationNumber(userRequest.getCompanyRegistrationNumber());
            user.setCompanyAddress1(userRequest.getCompanyAddress1());
            user.setCompanyAddress2(userRequest.getCompanyAddress2());
            user.setCompanyCountry(userRequest.getCompanyCountry());
            user.setCompanyPincode(userRequest.getCompanyPincode());
            user.setTaxId(userRequest.getTaxId());
        }
        
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setStatus("A"); 
        user.setCreatedBy(getAuditCode(createdBy));
        user.setUpdatedBy(getAuditCode(createdBy));
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        log.info("User created successfully with ID: {} for tenant: {}", savedUser.getUserId(), tenantId);
        
        return mapToDTO(savedUser);
    }
    
    @Override
    public UserResponse updateUserInTenant(Integer tenantId, Long userId, TenantUserUpdateRequest updateRequest, String updatedBy) {
        
        return updateUser(tenantId, userId, updatedBy, updateRequest);
    }
    
    @Override
    public void deleteUserFromTenant(Integer tenantId, Long userId) {
        
        deleteUser(tenantId, userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsersByTenant(Integer tenantId) {
        return userRepository.findByTenantIdAndStatus(tenantId, "A")
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getTenantAdmins(Integer tenantId) {
        return userRepository.findByTenantIdAndUserRole(tenantId, com.inventrik.digitalestore.domain.user.UserRole.TENANT_ADMIN)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    
    @Override
    public UserResponse mapToUserResponse(User user) {
        return mapToDTO(user);
    }
    
}