package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CertificateChallengeLoginRequest {
    @NotBlank
    private String challenge;

    @NotBlank
    private String signature;
}
