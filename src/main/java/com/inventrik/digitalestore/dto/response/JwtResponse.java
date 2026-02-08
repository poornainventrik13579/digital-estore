package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String userId;
    private String userRole;
    private Integer tenantId;
    private Instant expiresAt;
}
