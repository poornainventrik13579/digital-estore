package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.order.OrderItem;
import com.inventrik.digitalestore.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByTenantIdAndOrderId(Integer tenantId, Long orderId);

    // Fetch orders with items to avoid N+1 queries
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.tenantId = :tenantId")
    List<Order> findByTenantIdWithItems(@Param("tenantId") Integer tenantId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.tenantId = :tenantId AND o.userId = :userId")
    List<Order> findByTenantIdAndUserIdWithItems(@Param("tenantId") Integer tenantId, @Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.tenantId = :tenantId AND o.status = :status")
    List<Order> findByTenantIdAndStatusWithItems(@Param("tenantId") Integer tenantId, @Param("status") String status);

    // Keep original methods for cases where items aren't needed
    List<Order> findByTenantId(Integer tenantId);
    List<Order> findByTenantIdAndUserId(Integer tenantId, Long userId);
    List<Order> findByTenantIdAndStatus(Integer tenantId, String status);
    
    void deleteByTenantIdAndOrderId(Integer tenantId, Long orderId);
    
    @Query("SELECT oi FROM Order o JOIN o.orderItems oi WHERE o.tenantId = :tenantId AND o.orderId = :orderId AND oi.orderItemId = :orderItemId")
    Optional<OrderItem> findOrderItemByTenantIdAndOrderIdAndOrderItemId(
        @Param("tenantId") Integer tenantId, 
        @Param("orderId") Long orderId, 
        @Param("orderItemId") Long orderItemId);
    
    @Query("SELECT COUNT(oi) > 0 FROM Order o JOIN o.orderItems oi WHERE o.tenantId = :tenantId AND o.userId = :userId AND oi.productId = :productId AND o.status IN (:completedStatus, :processingStatus)")
    boolean hasUserPurchasedProduct(@Param("tenantId") Integer tenantId, @Param("userId") Long userId, @Param("productId") Long productId, @Param("completedStatus") String completedStatus, @Param("processingStatus") String processingStatus);

    @Query("SELECT o FROM Order o JOIN o.orderItems oi WHERE o.tenantId = :tenantId AND oi.orderItemId = :orderItemId")
    Optional<Order> findOrderByTenantIdAndOrderItemId(@Param("tenantId") Integer tenantId, @Param("orderItemId") Long orderItemId);
}