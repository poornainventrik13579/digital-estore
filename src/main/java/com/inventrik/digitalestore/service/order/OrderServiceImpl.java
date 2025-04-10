package com.inventrik.digitalestore.service.order;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.order.OrderItem;
import com.inventrik.digitalestore.domain.order.OrderStatus;
import com.inventrik.digitalestore.dto.request.OrderItemRequest;
import com.inventrik.digitalestore.dto.request.OrderRequest;
import com.inventrik.digitalestore.dto.request.OrderUpdateRequest;
import com.inventrik.digitalestore.dto.response.OrderItemResponse;
import com.inventrik.digitalestore.dto.response.OrderResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    
    // Utility method to convert Order entity to OrderResponse DTO
    private OrderResponse mapToDTO(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(this::mapToOrderItemDTO)
                .collect(Collectors.toList());
        
        return new OrderResponse(
            order.getOrderId(),
            order.getTenantId(),
            order.getUserId(),
            order.getOrderDate(),
            order.getCurrency(),
            order.getTotalAmount(),
            order.getExchangeRate(),
            order.getStatus(),
            order.getCreated(),
            order.getUpdated(),
            orderItemResponses
        );
    }
    
    // Utility method to convert OrderItem entity to OrderItemResponse DTO
    private OrderItemResponse mapToOrderItemDTO(OrderItem orderItem) {
        return new OrderItemResponse(
            orderItem.getOrderItemId(),
            orderItem.getOrderId(),
            orderItem.getProductId(),
            orderItem.getPriceAtPurchase(),
            orderItem.getLicenseKey(),
            orderItem.getStatus(),
            orderItem.getCreated(),
            orderItem.getUpdated()
        );
    }
    
    @Override
    public List<OrderResponse> getAllOrders(Integer tenantId) {
        return orderRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public OrderResponse getOrder(Integer tenantId, Long orderId) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToDTO(order);
    }
    
    @Override
    @Transactional
    public OrderResponse createOrder(Integer tenantId, String username, OrderRequest orderRequest) {
        // Generate a new order ID (in production, use a better ID generation strategy)
        Long newOrderId = System.currentTimeMillis();
        
        Order order = new Order();
        order.setTenantId(tenantId);
        order.setOrderId(newOrderId);
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setCurrency(orderRequest.getCurrency());
        order.setTotalAmount(orderRequest.getTotalAmount());
        order.setExchangeRate(orderRequest.getExchangeRate());
        order.setStatus(OrderStatus.PENDING.getDisplayName());
        // Ensure username is truncated to 2 characters for DB constraints
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        order.setCreatedBy(truncatedUsername);
        order.setUpdatedBy(truncatedUsername);
        
        // Set order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setTenantId(tenantId);
            orderItem.setOrderId(newOrderId);
            orderItem.setOrderItemId(System.currentTimeMillis() + orderItems.size()); // Simple unique ID generation
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setPriceAtPurchase(itemRequest.getPriceAtPurchase());
            orderItem.setLicenseKey(itemRequest.getLicenseKey());
            orderItem.setStatus("0"); // Active status
            orderItem.setCreatedBy(truncatedUsername);
            orderItem.setUpdatedBy(truncatedUsername);
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }
        
        order.setOrderItems(orderItems);
        
        Order savedOrder = orderRepository.save(order);
        
        return mapToDTO(savedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse updateOrder(Integer tenantId, Long orderId, String username, OrderUpdateRequest updateRequest) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // Only allow updating the status
        if (updateRequest.getStatus() != null) {
            // Validate status is a valid OrderStatus
            try {
                OrderStatus.fromDisplayName(updateRequest.getStatus());
                order.setStatus(updateRequest.getStatus());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid order status: " + updateRequest.getStatus());
            }
        }
        
        // Ensure username is truncated to 2 characters for DB constraints
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        order.setUpdatedBy(truncatedUsername);
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional
    public void deleteOrder(Integer tenantId, Long orderId) {
        if (!orderRepository.findByTenantIdAndOrderId(tenantId, orderId).isPresent()) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        
        orderRepository.deleteByTenantIdAndOrderId(tenantId, orderId);
    }
    
    @Override
    public List<OrderResponse> getOrdersByUser(Integer tenantId, Long userId) {
        return orderRepository.findByTenantIdAndUserId(tenantId, userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrderResponse> getOrdersByStatus(Integer tenantId, String status) {
        return orderRepository.findByTenantIdAndStatus(tenantId, status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public OrderResponse completeOrder(Integer tenantId, Long orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // Check if order can be completed
        if (OrderStatus.CANCELLED.getDisplayName().equals(order.getStatus()) || 
            OrderStatus.REFUNDED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Cannot complete order that is cancelled or refunded");
        }
        
        order.setStatus(OrderStatus.COMPLETED.getDisplayName());
        
        // Ensure username is truncated to 2 characters for DB constraints
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        order.setUpdatedBy(truncatedUsername);
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse cancelOrder(Integer tenantId, Long orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // Check if order can be cancelled
        if (OrderStatus.COMPLETED.getDisplayName().equals(order.getStatus()) || 
            OrderStatus.REFUNDED.getDisplayName().equals(order.getStatus()) ||
            OrderStatus.PARTIALLY_REFUNDED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Cannot cancel order that is completed or refunded");
        }
        
        order.setStatus(OrderStatus.CANCELLED.getDisplayName());
        
        // Ensure username is truncated to 2 characters for DB constraints
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        order.setUpdatedBy(truncatedUsername);
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse refundOrder(Integer tenantId, Long orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // Check if order can be refunded
        if (!OrderStatus.COMPLETED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Only completed orders can be refunded");
        }
        
        order.setStatus(OrderStatus.REFUNDED.getDisplayName());
        
        // Ensure username is truncated to 2 characters for DB constraints
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        order.setUpdatedBy(truncatedUsername);
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        return mapToDTO(updatedOrder);
    }
}