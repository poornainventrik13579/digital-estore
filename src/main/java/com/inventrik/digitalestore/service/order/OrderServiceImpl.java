package com.inventrik.digitalestore.service.order;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.order.OrderItem;
import com.inventrik.digitalestore.domain.order.OrderStatus;
import com.inventrik.digitalestore.dto.request.OrderItemRequest;
import com.inventrik.digitalestore.dto.request.OrderRequest;
import com.inventrik.digitalestore.dto.request.OrderUpdateRequest;
import com.inventrik.digitalestore.dto.response.OrderItemResponse;
import com.inventrik.digitalestore.dto.response.OrderResponse;
import com.inventrik.digitalestore.event.OrderStatusChangeEvent;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.OrderRepository;
import com.inventrik.digitalestore.repository.ProductRepository;
import com.inventrik.digitalestore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    
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
        // Generate a new order ID
        Long newOrderId = System.currentTimeMillis();
        
        // Verify user exists
        userRepository.findByTenantIdAndUserId(tenantId, orderRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + orderRequest.getUserId()));
        
        Order order = new Order();
        order.setTenantId(tenantId);
        order.setOrderId(newOrderId);
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setCurrency(orderRequest.getCurrency());
        order.setTotalAmount(orderRequest.getTotalAmount());
        order.setExchangeRate(orderRequest.getExchangeRate());
        order.setStatus(OrderStatus.PENDING.getDisplayName());
        
        // Ensure username is truncated to 2 characters
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        order.setCreatedBy(truncatedUsername);
        order.setUpdatedBy(truncatedUsername);
        
        // Save order first to get the ID
        Order savedOrder = orderRepository.save(order);
        
        // Set order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            // Verify product exists
            productRepository.findByTenantIdAndProductId(tenantId, itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId()));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setTenantId(tenantId);
            orderItem.setOrderId(newOrderId);
            orderItem.setOrderItemId(System.currentTimeMillis() + orderItems.size());
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setPriceAtPurchase(itemRequest.getPriceAtPurchase());
            orderItem.setLicenseKey(itemRequest.getLicenseKey());
            orderItem.setStatus("0"); // Active
            orderItem.setCreatedBy(truncatedUsername);
            orderItem.setUpdatedBy(truncatedUsername);
            orderItem.setCreated(LocalDateTime.now());
            orderItem.setUpdated(LocalDateTime.now());
            orderItems.add(orderItem);
        }
        
        // Add items to the order
        for (OrderItem item : orderItems) {
            savedOrder.getOrderItems().add(item);
            item.setOrder(savedOrder);
        }
        
        // Save again with items
        savedOrder = orderRepository.save(savedOrder);
        
        // Publish order created event (optional)
        eventPublisher.publishEvent(new OrderStatusChangeEvent(savedOrder, null, savedOrder.getStatus()));
        
        return mapToDTO(savedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse updateOrder(Integer tenantId, Long orderId, String username, OrderUpdateRequest updateRequest) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        String oldStatus = order.getStatus();
        
        if (updateRequest.getStatus() != null) {
            try {
                OrderStatus.fromDisplayName(updateRequest.getStatus());
                order.setStatus(updateRequest.getStatus());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid order status: " + updateRequest.getStatus());
            }
        }
        
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        order.setUpdatedBy(truncatedUsername);
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        // If status has changed, publish an event
        if (!oldStatus.equals(updatedOrder.getStatus())) {
            eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        }
        
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
        
        if (OrderStatus.CANCELLED.getDisplayName().equals(order.getStatus()) || 
            OrderStatus.REFUNDED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Cannot complete order that is cancelled or refunded");
        }
        
        String oldStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED.getDisplayName());
        
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        order.setUpdatedBy(truncatedUsername);
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        // Publish status change event
        eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse cancelOrder(Integer tenantId, Long orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (OrderStatus.COMPLETED.getDisplayName().equals(order.getStatus()) || 
            OrderStatus.REFUNDED.getDisplayName().equals(order.getStatus()) ||
            OrderStatus.PARTIALLY_REFUNDED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Cannot cancel order that is completed or refunded");
        }
        
        String oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED.getDisplayName());
        
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        order.setUpdatedBy(truncatedUsername);
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        // Publish status change event
        eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse refundOrder(Integer tenantId, Long orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (!OrderStatus.COMPLETED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Only completed orders can be refunded");
        }
        
        String oldStatus = order.getStatus();
        order.setStatus(OrderStatus.REFUNDED.getDisplayName());
        
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        order.setUpdatedBy(truncatedUsername);
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        // Publish status change event
        eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPurchasedProduct(Integer tenantId, Long userId, Long productId) {
        return orderRepository.hasUserPurchasedProduct(tenantId, userId, productId);
    }
}