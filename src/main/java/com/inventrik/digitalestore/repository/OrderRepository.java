package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find order by tenant and order ID
    Optional<Order> findByTenantIdAndOrderId(Integer tenantId, Long orderId);
    
    // Find all orders for a tenant
    List<Order> findByTenantId(Integer tenantId);
    
    // Find orders by tenant and user ID
    List<Order> findByTenantIdAndUserId(Integer tenantId, Long userId);
    
    // Find orders by tenant and status
    List<Order> findByTenantIdAndStatus(Integer tenantId, String status);
    
    // Delete order by tenant and order ID
    void deleteByTenantIdAndOrderId(Integer tenantId, Long orderId);
}