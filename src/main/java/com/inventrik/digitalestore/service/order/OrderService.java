package com.inventrik.digitalestore.service.order;

import com.inventrik.digitalestore.dto.request.OrderRequest;
import com.inventrik.digitalestore.dto.request.OrderUpdateRequest;
import com.inventrik.digitalestore.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    // Get all orders for a tenant with optional filters (userId, status)
    List<OrderResponse> getAllOrders(Integer tenantId, Long userId, String status);

    // Get a single order by ID
    OrderResponse getOrder(Integer tenantId, Long orderId);

    // Create a new order
    OrderResponse createOrder(Integer tenantId, String username, OrderRequest orderRequest);

    // Update an existing order (e.g., change status)
    OrderResponse updateOrder(Integer tenantId, Long orderId, String username, OrderUpdateRequest updateRequest);

    // Delete an order
    void deleteOrder(Integer tenantId, Long orderId);

    // Complete an order
    OrderResponse completeOrder(Integer tenantId, Long orderId, String username);

    // Cancel an order
    OrderResponse cancelOrder(Integer tenantId, Long orderId, String username);

    // Refund an order (full refund)
    OrderResponse refundOrder(Integer tenantId, Long orderId, String username);

    // Check if user has purchased a specific product
    boolean hasUserPurchasedProduct(Integer tenantId, Long userId, Long productId);
}