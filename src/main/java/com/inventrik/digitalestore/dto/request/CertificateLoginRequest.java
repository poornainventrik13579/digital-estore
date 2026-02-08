package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CertificateLoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String authType;
}
