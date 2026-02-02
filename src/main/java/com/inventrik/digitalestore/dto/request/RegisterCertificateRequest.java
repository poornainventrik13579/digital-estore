package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterCertificateRequest {
    @NotBlank
    private String publicKey;
}
