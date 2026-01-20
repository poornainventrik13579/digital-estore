package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.product.DigitalProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DigitalProductDetailsRepository extends JpaRepository<DigitalProductDetails, DigitalProductDetails.DigitalProductDetailsPK> {

    // Find digital product details by tenant and product ID
    Optional<DigitalProductDetails> findByTenantIdAndProductId(Integer tenantId, String productId);

    // Find digital product details by product ID only
    Optional<DigitalProductDetails> findByProductId(String productId);

    // Find all digital product details for a tenant
    List<DigitalProductDetails> findByTenantId(Integer tenantId);

    // Find active digital product details for a tenant
    List<DigitalProductDetails> findByTenantIdAndStatus(Integer tenantId, String status);

    // Find by file format
    List<DigitalProductDetails> findByTenantIdAndFileFormat(Integer tenantId, String fileFormat);

    // Find by version
    List<DigitalProductDetails> findByTenantIdAndVersion(Integer tenantId, String version);

    // Check if digital product details exist for a product
    boolean existsByTenantIdAndProductId(Integer tenantId, String productId);

    // Delete by tenant and product ID
    void deleteByTenantIdAndProductId(Integer tenantId, String productId);
}