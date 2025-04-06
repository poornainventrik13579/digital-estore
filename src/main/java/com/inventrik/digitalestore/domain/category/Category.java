package com.inventrik.digitalestore.domain.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status;
    
    @Column(name = "created_by", nullable = false, length = 2)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 2)
    private String updatedBy;
    
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