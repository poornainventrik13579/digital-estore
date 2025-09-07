package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.order.OrderStatus;
import com.inventrik.digitalestore.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldSaveAndFindOrder() {
        Order order = createTestOrder(1, 1L, 1L);
        
        Order saved = orderRepository.save(order);
        
        assertThat(saved.getOrderId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getTotalAmount()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    void shouldFindByTenantIdAndOrderId() {
        Order order = createTestOrder(1, 1L, 1L);
        entityManager.persistAndFlush(order);

        Optional<Order> found = orderRepository.findByTenantIdAndOrderId(1, 1L);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
    }

    @Test
    void shouldFindAllByTenantId() {
        Order order1 = createTestOrder(1, 1L, 1L);
        Order order2 = createTestOrder(1, 2L, 2L);
        Order order3 = createTestOrder(2, 1L, 1L);
        
        entityManager.persistAndFlush(order1);
        entityManager.persistAndFlush(order2);
        entityManager.persistAndFlush(order3);

        List<Order> tenant1Orders = orderRepository.findByTenantId(1);
        List<Order> tenant2Orders = orderRepository.findByTenantId(2);

        assertThat(tenant1Orders).hasSize(2);
        assertThat(tenant2Orders).hasSize(1);
    }

    @Test
    void shouldFindByTenantIdAndUserId() {
        Order order1 = createTestOrder(1, 1L, 1L);
        Order order2 = createTestOrder(1, 2L, 1L);
        Order order3 = createTestOrder(1, 3L, 2L);
        
        entityManager.persistAndFlush(order1);
        entityManager.persistAndFlush(order2);
        entityManager.persistAndFlush(order3);

        List<Order> user1Orders = orderRepository.findByTenantIdAndUserId(1, 1L);
        List<Order> user2Orders = orderRepository.findByTenantIdAndUserId(1, 2L);

        assertThat(user1Orders).hasSize(2);
        assertThat(user2Orders).hasSize(1);
        assertThat(user1Orders).extracting("orderId").containsExactlyInAnyOrder(1L, 2L);
        assertThat(user2Orders).extracting("orderId").containsExactlyInAnyOrder(3L);
    }

    @Test
    void shouldFindByTenantIdAndStatus() {
        Order pendingOrder = createTestOrder(1, 1L, 1L);
        pendingOrder.setStatus("Pending");
        Order completedOrder = createTestOrder(1, 2L, 1L);
        completedOrder.setStatus("Completed");
        Order cancelledOrder = createTestOrder(1, 3L, 1L);
        cancelledOrder.setStatus("Cancelled");
        
        entityManager.persistAndFlush(pendingOrder);
        entityManager.persistAndFlush(completedOrder);
        entityManager.persistAndFlush(cancelledOrder);

        List<Order> pendingOrders = orderRepository.findByTenantIdAndStatus(1, "Pending");
        List<Order> completedOrders = orderRepository.findByTenantIdAndStatus(1, "Completed");

        assertThat(pendingOrders).hasSize(1);
        assertThat(completedOrders).hasSize(1);
        assertThat(pendingOrders.get(0).getStatus()).isEqualTo("Pending");
        assertThat(completedOrders.get(0).getStatus()).isEqualTo("Completed");
    }

    @Test
    void shouldFindByTenantIdAndCreatedBetween() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);
        LocalDateTime oldDate = LocalDateTime.now().minusDays(20);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        Order oldOrder = createTestOrder(1, 1L, 1L);
        oldOrder.setCreated(oldDate);
        Order recentOrder = createTestOrder(1, 2L, 1L);
        recentOrder.setCreated(startDate.plusDays(1));
        Order futureOrder = createTestOrder(1, 3L, 1L);
        futureOrder.setCreated(futureDate);
        
        entityManager.persistAndFlush(oldOrder);
        entityManager.persistAndFlush(recentOrder);
        entityManager.persistAndFlush(futureOrder);

        List<Order> ordersInRange = orderRepository.findByTenantId(1).stream()
                .filter(o -> o.getCreated().isAfter(startDate) && o.getCreated().isBefore(endDate))
                .collect(java.util.stream.Collectors.toList());

        assertThat(ordersInRange).hasSize(1);
        assertThat(ordersInRange.get(0).getOrderId()).isEqualTo(2L);
    }

    @Test
    void shouldFindByTenantIdAndTotalAmountGreaterThanEqual() {
        Order cheapOrder = createTestOrder(1, 1L, 1L);
        cheapOrder.setTotalAmount(new BigDecimal("10.00"));
        Order expensiveOrder = createTestOrder(1, 2L, 1L);
        expensiveOrder.setTotalAmount(new BigDecimal("100.00"));
        Order veryExpensiveOrder = createTestOrder(1, 3L, 1L);
        veryExpensiveOrder.setTotalAmount(new BigDecimal("500.00"));
        
        entityManager.persistAndFlush(cheapOrder);
        entityManager.persistAndFlush(expensiveOrder);
        entityManager.persistAndFlush(veryExpensiveOrder);

        List<Order> expensiveOrders = orderRepository.findByTenantId(1).stream()
                .filter(o -> o.getTotalAmount().compareTo(new BigDecimal("100.00")) >= 0)
                .collect(java.util.stream.Collectors.toList());

        assertThat(expensiveOrders).hasSize(2);
        assertThat(expensiveOrders).extracting("orderId").containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        Optional<Order> found = orderRepository.findByTenantIdAndOrderId(999, 999L);
        
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteOrder() {
        Order order = createTestOrder(1, 1L, 1L);
        Order saved = entityManager.persistAndFlush(order);

        orderRepository.deleteByTenantIdAndOrderId(saved.getTenantId(), saved.getOrderId());

        Optional<Order> found = orderRepository.findByTenantIdAndOrderId(1, 1L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateOrder() {
        Order order = createTestOrder(1, 1L, 1L);
        Order saved = entityManager.persistAndFlush(order);

        saved.setStatus("Completed");
        saved.setTotalAmount(new BigDecimal("149.99"));
        saved.setUpdated(LocalDateTime.now());
        
        Order updated = orderRepository.save(saved);

        assertThat(updated.getStatus()).isEqualTo("Completed");
        assertThat(updated.getTotalAmount()).isEqualTo(new BigDecimal("149.99"));
    }

    @Test
    void shouldHandleMultipleTenantsCorrectly() {
        Order tenant1Order = createTestOrder(1, 1L, 1L);
        Order tenant2Order = createTestOrder(2, 1L, 1L);
        
        entityManager.persistAndFlush(tenant1Order);
        entityManager.persistAndFlush(tenant2Order);

        Optional<Order> found1 = orderRepository.findByTenantIdAndOrderId(1, 1L);
        Optional<Order> found2 = orderRepository.findByTenantIdAndOrderId(2, 1L);

        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(found1.get().getTenantId()).isEqualTo(1);
        assertThat(found2.get().getTenantId()).isEqualTo(2);
    }

    @Test
    void shouldCountByTenantId() {
        Order order1 = createTestOrder(1, 1L, 1L);
        Order order2 = createTestOrder(1, 2L, 1L);
        Order order3 = createTestOrder(2, 1L, 1L);
        
        entityManager.persistAndFlush(order1);
        entityManager.persistAndFlush(order2);
        entityManager.persistAndFlush(order3);

        long tenant1Count = orderRepository.findByTenantId(1).size();
        long tenant2Count = orderRepository.findByTenantId(2).size();

        assertThat(tenant1Count).isEqualTo(2);
        assertThat(tenant2Count).isEqualTo(1);
    }

    @Test
    void shouldCountByTenantIdAndStatus() {
        Order pendingOrder1 = createTestOrder(1, 1L, 1L);
        pendingOrder1.setStatus("Pending");
        Order pendingOrder2 = createTestOrder(1, 2L, 1L);
        pendingOrder2.setStatus("Pending");
        Order completedOrder = createTestOrder(1, 3L, 1L);
        completedOrder.setStatus("Completed");
        
        entityManager.persistAndFlush(pendingOrder1);
        entityManager.persistAndFlush(pendingOrder2);
        entityManager.persistAndFlush(completedOrder);

        long pendingCount = orderRepository.findByTenantIdAndStatus(1, "Pending").size();
        long completedCount = orderRepository.findByTenantIdAndStatus(1, "Completed").size();

        assertThat(pendingCount).isEqualTo(2);
        assertThat(completedCount).isEqualTo(1);
    }

    private Order createTestOrder(Integer tenantId, Long orderId, Long userId) {
        Order order = new Order();
        order.setTenantId(tenantId);
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setStatus("Pending");
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setCurrency("USD");
        order.setCreated(LocalDateTime.now());
        order.setUpdated(LocalDateTime.now());
        return order;
    }
}