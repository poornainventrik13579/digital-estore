package com.inventrik.digitalestore.domain.page;

import lombok.Data;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "pages")
@IdClass(Page.PagePK.class)
@Data
public class Page {

    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Id
    @Column(name = "page_id")
    private String pageId;

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

    @Column(name = "template", nullable = false, length = 50)
    private String template = "default";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "visibility", nullable = false, length = 20)
    private String visibility = "PUBLIC";

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "language", length = 10)
    private String language = "en";

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }

    public static class PagePK implements Serializable {
        private Integer tenantId;
        private String pageId;

        public PagePK() {}

        public PagePK(Integer tenantId, String pageId) {
            this.tenantId = tenantId;
            this.pageId = pageId;
        }

        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public String getPageId() { return pageId; }
        public void setPageId(String pageId) { this.pageId = pageId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PagePK)) return false;
            PagePK that = (PagePK) o;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(pageId, that.pageId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenantId, pageId);
        }
    }
}
