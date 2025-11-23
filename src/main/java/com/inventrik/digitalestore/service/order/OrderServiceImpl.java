package com.inventrik.digitalestore.service.order;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.order.OrderItem;
import com.inventrik.digitalestore.domain.order.OrderStatus;
import com.inventrik.digitalestore.domain.product.Product;
import com.inventrik.digitalestore.dto.request.OrderItemRequest;
import com.inventrik.digitalestore.dto.request.OrderRequest;
import com.inventrik.digitalestore.dto.request.OrderUpdateRequest;
import com.inventrik.digitalestore.dto.request.ValidateDiscountRequest;
import com.inventrik.digitalestore.dto.response.DiscountValidationResponse;
import com.inventrik.digitalestore.dto.response.OrderItemResponse;
import com.inventrik.digitalestore.dto.response.OrderResponse;
import com.inventrik.digitalestore.event.OrderStatusChangeEvent;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.OrderRepository;
import com.inventrik.digitalestore.repository.ProductRepository;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.discount.DiscountService;
import com.inventrik.digitalestore.service.user.UserService;
import com.inventrik.digitalestore.service.currency.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final DiscountService discountService;
    private final IdGeneratorService idGeneratorService;
    private final UserService userService;
    private final CurrencyService currencyService;
    
    private OrderResponse mapToDTO(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(this::mapToOrderItemDTO)
                .collect(Collectors.toList());
        
        return new OrderResponse(
            order.getOrderId(),
            order.getTenantId(),
            order.getUserId(),
            order.getOrderNumber(),
            order.getOrderDate(),
            order.getCurrencyCode(),
            order.getSubtotal(),
            order.getTaxAmount(),
            order.getShippingAmount(),
            order.getDiscountAmount(),
            order.getTotalAmount(),
            order.getExchangeRate(),
            order.getStatus(),
            order.getCreated(),
            order.getUpdated(),
            orderItemResponses
        );
    }
    
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
        // Use optimized query with JOIN FETCH to avoid N+1 problem
        return orderRepository.findByTenantIdWithItems(tenantId).stream()
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
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderResponse createOrder(Integer tenantId, String username, OrderRequest orderRequest) {
        
        userRepository.findByTenantIdAndUserId(tenantId, orderRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + orderRequest.getUserId()));

        List<OrderItem> orderItems = new ArrayList<>();
        Long tempOrderId = 0L;

        for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            Product product = productRepository.findByTenantIdAndProductId(tenantId, itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId()));

            BigDecimal actualPrice = product.getDefaultPrice();
            if (itemRequest.getPriceAtPurchase().compareTo(actualPrice) != 0) {
                throw new BusinessException("Price mismatch for product " + product.getProductName() +
                    ". Expected: " + actualPrice + ", Provided: " + itemRequest.getPriceAtPurchase());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setTenantId(tenantId);
            orderItem.setOrderId(tempOrderId);
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setPriceAtPurchase(actualPrice);
            orderItem.setLicenseKey(itemRequest.getLicenseKey());
            orderItem.setStatus("0");
            orderItems.add(orderItem);
        }

        BigDecimal itemsTotal = orderItems.stream()
                .map(OrderItem::getPriceAtPurchase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (itemsTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Order items total must be greater than zero");
        }

        if (itemsTotal.compareTo(orderRequest.getTotalAmount()) != 0) {
            throw new BusinessException("Order total mismatch. Expected: " + itemsTotal + ", Provided: " + orderRequest.getTotalAmount());
        }

        BigDecimal exchangeRate = currencyService.getExchangeRate("USD", orderRequest.getCurrency(), tenantId);

        BigDecimal discountAmount = BigDecimal.ZERO;

        if (orderRequest.getDiscountCode() != null && !orderRequest.getDiscountCode().trim().isEmpty()) {

            DiscountValidationResponse validation = discountService.validateDiscountCode(tenantId,
                new ValidateDiscountRequest(orderRequest.getDiscountCode().trim(), itemsTotal, orderRequest.getUserId()));

            if (!validation.isValid()) {
                throw new BusinessException("Invalid discount code: " + validation.getMessage());
            }
        }

        Long newOrderId = idGeneratorService.generateId(tenantId, "ORDER");

        if (orderRequest.getDiscountCode() != null && !orderRequest.getDiscountCode().trim().isEmpty()) {
            try {
                discountAmount = discountService.applyDiscountToOrder(
                    tenantId,
                    orderRequest.getDiscountCode().trim(),
                    newOrderId,
                    orderRequest.getUserId(),
                    itemsTotal,
                    username
                );
            } catch (Exception e) {
                throw new BusinessException("Failed to apply discount code: " + e.getMessage());
            }
        }

        BigDecimal finalAmount = itemsTotal.subtract(discountAmount);

        Order order = new Order();
        order.setTenantId(tenantId);
        order.setOrderId(newOrderId);
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setCurrencyCode(orderRequest.getCurrency());
        order.setOrderNumber("ORD-" + newOrderId);
        order.setSubtotal(itemsTotal);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(finalAmount);
        order.setExchangeRate(exchangeRate);
        order.setStatus(OrderStatus.PENDING.getDisplayName());

        order.setCreatedBy(userService.getAuditCode(username));
        order.setUpdatedBy(userService.getAuditCode(username));

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(newOrderId);
            item.setOrderItemId(idGeneratorService.generateId(tenantId, "ORDER_ITEM"));
            item.setCreatedBy(userService.getAuditCode(username));
            item.setUpdatedBy(userService.getAuditCode(username));
            item.setCreated(LocalDateTime.now());
            item.setUpdated(LocalDateTime.now());
            savedOrder.getOrderItems().add(item);
            item.setOrder(savedOrder);
        }

        savedOrder = orderRepository.save(savedOrder);

        eventPublisher.publishEvent(new OrderStatusChangeEvent(savedOrder, null, savedOrder.getStatus()));

        return mapToDTO(savedOrder);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderResponse updateOrder(Integer tenantId, Long orderId, String username, OrderUpdateRequest updateRequest) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getCreatedBy().equals(userService.getAuditCode(username))) {
            throw new com.inventrik.digitalestore.exception.UnauthorizedException("You do not have permission to perform this action");
        }

        String oldStatus = order.getStatus();
        
        if (updateRequest.getStatus() != null) {
            try {
                OrderStatus.fromDisplayName(updateRequest.getStatus());
                order.setStatus(updateRequest.getStatus());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid order status: " + updateRequest.getStatus());
            }
        }
        
        order.setUpdatedBy(userService.getAuditCode(username));
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        if (!oldStatus.equals(updatedOrder.getStatus())) {
            eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        }
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteOrder(Integer tenantId, Long orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getCreatedBy().equals(userService.getAuditCode(username))) {
            throw new com.inventrik.digitalestore.exception.UnauthorizedException("You do not have permission to perform this action");
        }

        orderRepository.deleteByTenantIdAndOrderId(tenantId, orderId);
    }
    
    @Override
    public List<OrderResponse> getOrdersByUser(Integer tenantId, Long userId) {
        return orderRepository.findByTenantIdAndUserIdWithItems(tenantId, userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(Integer tenantId, String status) {
        return orderRepository.findByTenantIdAndStatusWithItems(tenantId, status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public OrderResponse completeOrder(Integer tenantId, Long orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getCreatedBy().equals(userService.getAuditCode(username))) {
            throw new com.inventrik.digitalestore.exception.UnauthorizedException(
                "You do not have permission to complete this order");
        }

        if (OrderStatus.CANCELLED.getDisplayName().equals(order.getStatus()) ||
            OrderStatus.REFUNDED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Cannot complete order that is cancelled or refunded");
        }
        
        String oldStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED.getDisplayName());
        
        order.setUpdatedBy(userService.getAuditCode(username));
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse cancelOrder(Integer tenantId, Long orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getCreatedBy().equals(userService.getAuditCode(username))) {
            throw new com.inventrik.digitalestore.exception.UnauthorizedException(
                "You do not have permission to cancel this order");
        }

        if (OrderStatus.COMPLETED.getDisplayName().equals(order.getStatus()) ||
            OrderStatus.REFUNDED.getDisplayName().equals(order.getStatus()) ||
            OrderStatus.PARTIALLY_REFUNDED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Cannot cancel order that is completed or refunded");
        }
        
        String oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED.getDisplayName());
        
        order.setUpdatedBy(userService.getAuditCode(username));
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse refundOrder(Integer tenantId, Long orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getCreatedBy().equals(userService.getAuditCode(username))) {
            throw new com.inventrik.digitalestore.exception.UnauthorizedException("You do not have permission to perform this action");
        }

        if (!OrderStatus.COMPLETED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Only completed orders can be refunded");
        }
        
        String oldStatus = order.getStatus();
        order.setStatus(OrderStatus.REFUNDED.getDisplayName());
        
        order.setUpdatedBy(userService.getAuditCode(username));
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPurchasedProduct(Integer tenantId, Long userId, Long productId) {
        // Check if user has purchased product in completed or processing orders
        return orderRepository.hasUserPurchasedProduct(tenantId, userId, productId,
                OrderStatus.COMPLETED.getDisplayName(), OrderStatus.PROCESSING.getDisplayName());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean doesUserOwnOrderItem(Integer tenantId, Long orderItemId, String username) {
        Order order = orderRepository.findOrderByTenantIdAndOrderItemId(tenantId, orderItemId)
                .orElse(null);

        if (order == null) {
            return false;
        }

        return userService.isCurrentUser(tenantId, order.getUserId(), username);
    }
}