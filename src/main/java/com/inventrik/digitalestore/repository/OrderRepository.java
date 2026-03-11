package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Order.OrderPK> {

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.tenantId = :tenantId AND o.orderId = :orderId")
    Optional<Order> findByTenantIdAndOrderId(@Param("tenantId") Integer tenantId, @Param("orderId") String orderId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.tenantId = :tenantId ORDER BY o.orderDate DESC")
    List<Order> findByTenantId(@Param("tenantId") Integer tenantId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.tenantId = :tenantId AND o.userId = :userId ORDER BY o.orderDate DESC")
    List<Order> findByTenantIdAndUserId(@Param("tenantId") Integer tenantId, @Param("userId") String userId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.tenantId = :tenantId AND o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByTenantIdAndStatus(@Param("tenantId") Integer tenantId, @Param("status") String status);

    void deleteByTenantIdAndOrderId(Integer tenantId, String orderId);

    @Query("SELECT oi FROM Order o JOIN o.orderItems oi WHERE o.tenantId = :tenantId AND o.orderId = :orderId AND oi.orderItemId = :orderItemId")
    Optional<OrderItem> findOrderItemByTenantIdAndOrderIdAndOrderItemId(
        @Param("tenantId") Integer tenantId,
        @Param("orderId") String orderId,
        @Param("orderItemId") String orderItemId);

    @Query("SELECT COUNT(oi) > 0 FROM Order o JOIN o.orderItems oi WHERE o.tenantId = :tenantId AND o.userId = :userId AND oi.productId = :productId AND o.status IN ('Completed', 'Processing')")
    boolean hasUserPurchasedProduct(@Param("tenantId") Integer tenantId, @Param("userId") String userId, @Param("productId") String productId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.tenantId = :tenantId AND o.userId = :userId AND o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByTenantIdAndUserIdAndStatus(@Param("tenantId") Integer tenantId, @Param("userId") String userId, @Param("status") String status);
}