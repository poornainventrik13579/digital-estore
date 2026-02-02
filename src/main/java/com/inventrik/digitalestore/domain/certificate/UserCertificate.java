package com.inventrik.digitalestore.domain.certificate;

import com.inventrik.digitalestore.domain.audit.AuditableEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_certificates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"session_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserCertificate extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 255)
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "session_id", nullable = false, unique = true, length = 255)
    private String sessionId;

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "active";

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (status == null) {
            status = "active";
        }
    }
}
