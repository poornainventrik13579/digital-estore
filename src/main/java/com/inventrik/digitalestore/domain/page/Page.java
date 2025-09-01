package com.inventrik.digitalestore.domain.page;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Page {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "slug", nullable = false, length = 100)
    private String slug;
    
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;
    
    @Column(name = "meta_title", length = 256)
    private String metaTitle;
    
    @Column(name = "meta_description", length = 256)
    private String metaDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PageStatus status = PageStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private PageVisibility visibility = PageVisibility.PUBLIC;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "language", length = 10)
    private String language = "en";
    
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;
    
    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = PageStatus.DRAFT;
        }
        if (visibility == null) {
            visibility = PageVisibility.PUBLIC;
        }
        if (isDefault == null) {
            isDefault = false;
        }
        if (language == null) {
            language = "en";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void publish() {
        this.status = PageStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
    
    public void archive() {
        this.status = PageStatus.ARCHIVED;
    }
    
    public boolean isPublished() {
        return status == PageStatus.PUBLISHED;
    }
    
    public boolean isVisible() {
        return visibility == PageVisibility.PUBLIC || 
               (visibility == PageVisibility.PRIVATE && isPublished());
    }
}
