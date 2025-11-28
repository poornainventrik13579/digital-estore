package com.inventrik.digitalestore.domain.category;

import com.inventrik.digitalestore.domain.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "categories")
@IdClass(Category.CategoryPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "category_id")
    private Long categoryId;
    
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
    
    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }
    
    public void removeProduct(Product product) {
        products.remove(product);
        product.setCategory(null);
    }
    
    public static class CategoryPK implements Serializable {
        private Integer tenantId;
        private Long categoryId;
        
        public CategoryPK() {}
        
        public CategoryPK(Integer tenantId, Long categoryId) {
            this.tenantId = tenantId;
            this.categoryId = categoryId;
        }
        
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CategoryPK)) return false;
            CategoryPK that = (CategoryPK) o;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(categoryId, that.categoryId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, categoryId);
        }
    }
}