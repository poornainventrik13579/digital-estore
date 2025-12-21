package com.inventrik.digitalestore.domain.theme;

import lombok.Data;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "store_themes")
@IdClass(StoreTheme.StoreThemePK.class)
@Data
public class StoreTheme {

    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Id
    @Column(name = "theme_id")
    private Long themeId;

    @Column(name = "theme_name", length = 100)
    private String themeName;

    @Column(name = "tagline", length = 256)
    private String tagline;

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "banner_image", length = 256)
    private String bannerImage;

    @Column(name = "join_cta", length = 256)
    private String joinCta;

    @Column(name = "copyright_text", length = 256)
    private String copyrightText;

    @Column(name = "hero_title", length = 256)
    private String heroTitle;

    @Column(name = "hero_description", columnDefinition =  "TEXT")
    private String heroDescription;

    @Column(name = "status", nullable = false, length = 2)
    private String status;

    @Column(name = "created_by", nullable = false, length = 2)
    private String createdBy;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;

    @Column(name = "updated")
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

    public static class StoreThemePK implements Serializable {
        private Integer tenantId;
        private Long themeId;

        public StoreThemePK() {}

        public StoreThemePK(Integer tenantId, Long themeId) {
            this.tenantId = tenantId;
            this.themeId = themeId;
        }

        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getThemeId() { return themeId; }
        public void setThemeId(Long themeId) { this.themeId = themeId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StoreThemePK)) return false;
            StoreThemePK that = (StoreThemePK) o;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(themeId, that.themeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenantId, themeId);
        }
    }
}
