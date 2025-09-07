package com.inventrik.digitalestore.service.order;

import com.inventrik.digitalestore.dto.request.OrderRequest;
import com.inventrik.digitalestore.dto.request.OrderUpdateRequest;
import com.inventrik.digitalestore.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    
    List<OrderResponse> getAllOrders(Integer tenantId);
    
    OrderResponse getOrder(Integer tenantId, Long orderId);
    
    OrderResponse createOrder(Integer tenantId, String username, OrderRequest orderRequest);
    
    OrderResponse updateOrder(Integer tenantId, Long orderId, String username, OrderUpdateRequest updateRequest);
    
    void deleteOrder(Integer tenantId, Long orderId);
    
    List<OrderResponse> getOrdersByUser(Integer tenantId, Long userId);
    
    List<OrderResponse> getOrdersByStatus(Integer tenantId, String status);
    
    OrderResponse completeOrder(Integer tenantId, Long orderId, String username);
    
    OrderResponse cancelOrder(Integer tenantId, Long orderId, String username);
    
    OrderResponse refundOrder(Integer tenantId, Long orderId, String username);
    
    boolean hasUserPurchasedProduct(Integer tenantId, Long userId, Long productId);
}