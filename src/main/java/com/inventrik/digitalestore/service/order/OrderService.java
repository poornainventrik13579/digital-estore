package com.inventrik.digitalestore.service.order;

import com.inventrik.digitalestore.dto.request.OrderRequest;
import com.inventrik.digitalestore.dto.request.OrderUpdateRequest;
import com.inventrik.digitalestore.dto.response.OrderResponse;
import com.inventrik.digitalestore.dto.response.PagedResponse;

public interface OrderService {

    PagedResponse<OrderResponse> getAllOrders(Integer tenantId, String username, boolean canAccessAllOrders,
                                              String status, int page, int size);

    OrderResponse getOrder(Integer tenantId, String orderId);

    OrderResponse createOrder(Integer tenantId, String username, OrderRequest orderRequest);

    OrderResponse updateOrder(Integer tenantId, String orderId, String username, OrderUpdateRequest updateRequest);

    void deleteOrder(Integer tenantId, String orderId);

    OrderResponse completeOrder(Integer tenantId, String orderId, String username);

    OrderResponse cancelOrder(Integer tenantId, String orderId, String username);

    OrderResponse refundOrder(Integer tenantId, String orderId, String username);

    boolean hasUserPurchasedProduct(Integer tenantId, String userId, String productId);
}