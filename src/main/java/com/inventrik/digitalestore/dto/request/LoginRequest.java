package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {
    // Optional: Required for tenant-specific users (store admins, customers)
    // Not required for platform/system admins
    private Integer tenantId;

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}