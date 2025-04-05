package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.request.UserUpdateRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.UserRepository;
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
    @Transactional
    public UserResponse createUser(Integer tenantId, String createdBy, UserRequest userRequest) {
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
        Long newUserId = System.currentTimeMillis();
        
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
        user.setCreatedBy(createdBy.length() > 2 ? createdBy.substring(0, 2) : createdBy);
        // Ensure updatedBy doesn't exceed 2 characters
        user.setUpdatedBy(createdBy.length() > 2 ? createdBy.substring(0, 2) : createdBy);
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        return mapToDTO(savedUser);
    }
    
    @Override
    @Transactional
    public UserResponse updateUser(Integer tenantId, Long userId, String updatedBy, UserUpdateRequest updateRequest) {
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
        user.setUpdatedBy(updatedBy.length() > 2 ? updatedBy.substring(0, 2) : updatedBy);
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
}