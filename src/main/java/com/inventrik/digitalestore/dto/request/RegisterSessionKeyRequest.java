package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterSessionKeyRequest {
    @NotBlank
    private String sessionPublicKey;

    @NotBlank
    private String masterSignature;

    @NotNull
    private Long expiresAt;
}
