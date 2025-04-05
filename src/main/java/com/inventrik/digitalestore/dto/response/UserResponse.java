package com.inventrik.digitalestore.dto.response;

import com.inventrik.digitalestore.domain.user.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private Integer tenantId;
    private String username;
    private String firstName;
    private String lastName;
    private String image;
    private String phone;
    private String email;
    private UserType userType;
    private String companyName;
    private String companyRegistrationNumber;
    private String companyAddress1;
    private String companyAddress2;
    private String companyCountry;
    private String companyPincode;
    private String taxId;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
}