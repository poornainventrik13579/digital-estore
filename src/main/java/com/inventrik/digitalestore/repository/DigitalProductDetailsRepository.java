package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.product.DigitalProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DigitalProductDetailsRepository extends JpaRepository<DigitalProductDetails, Long> {
    
    Optional<DigitalProductDetails> findByTenantIdAndProductId(Integer tenantId, Long productId);

    List<DigitalProductDetails> findByTenantId(Integer tenantId);
    
    List<DigitalProductDetails> findByTenantIdAndStatus(Integer tenantId, String status);
    
    List<DigitalProductDetails> findByTenantIdAndFileFormat(Integer tenantId, String fileFormat);
    
    List<DigitalProductDetails> findByTenantIdAndVersion(Integer tenantId, String version);
    
    boolean existsByTenantIdAndProductId(Integer tenantId, Long productId);
    
    void deleteByTenantIdAndProductId(Integer tenantId, Long productId);
}