package com.inventrik.digitalestore.domain.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity {
    
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
        if (createdBy == null) {
            createdBy = "system"; // Default value
        }
        if (updatedBy == null) {
            updatedBy = "system"; // Default value
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
        if (updatedBy == null) {
            updatedBy = "system"; // Default value
        }
    }
} 