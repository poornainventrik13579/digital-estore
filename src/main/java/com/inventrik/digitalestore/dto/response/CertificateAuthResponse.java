package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CertificateAuthResponse {
    private String message;
    private String userId;
    private String sessionId;
}
