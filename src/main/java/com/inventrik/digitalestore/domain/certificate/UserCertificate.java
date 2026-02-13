package com.inventrik.digitalestore.domain.certificate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCertificate {

    @Id
    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "user_id", nullable = false, length = 32)
    private String userId;

    @Column(name = "public_key", nullable = false, length = 124)
    private String publicKey;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;

    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}
