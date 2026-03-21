package com.inventrik.digitalestore.service.order;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.order.OrderItem;
import com.inventrik.digitalestore.domain.order.OrderStatus;
import com.inventrik.digitalestore.dto.request.OrderItemRequest;
import com.inventrik.digitalestore.dto.request.OrderRequest;
import com.inventrik.digitalestore.dto.request.OrderUpdateRequest;
import com.inventrik.digitalestore.dto.response.OrderItemResponse;
import com.inventrik.digitalestore.dto.response.OrderResponse;
import com.inventrik.digitalestore.dto.response.PagedResponse;
import com.inventrik.digitalestore.event.OrderStatusChangeEvent;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.OrderRepository;
import com.inventrik.digitalestore.repository.ProductRepository;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.discount.DiscountService;
import com.inventrik.digitalestore.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final TenantRepository tenantRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DiscountService discountService;
    private final IdGeneratorService idGeneratorService;
    private final UserService userService;
    
    private OrderResponse mapToDTO(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(this::mapToOrderItemDTO)
                .collect(Collectors.toList());

        BigDecimal subTotal = order.getOrderItems().stream()
                .map(OrderItem::getPriceAtPurchase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = subTotal.subtract(order.getTotalAmount()).max(BigDecimal.ZERO);

        return new OrderResponse(
                order.getOrderId(),
                order.getTenantId(),
                order.getUserId(),
                order.getOrderDate(),
                order.getCurrency(),
                subTotal,
                discountAmount,
                BigDecimal.ZERO,
                order.getTotalAmount(),
                order.getExchangeRate(),
                order.getStatus(),
                orderItemResponses.size(),
                order.getCreated(),
                order.getUpdated(),
                orderItemResponses
        );
    }
    
    private OrderItemResponse mapToOrderItemDTO(OrderItem orderItem) {
        String productName = null;
        String productImageUrl = null;
  
        if (orderItem.getProduct() != null) {
            productName = orderItem.getProduct().getProductName();
            productImageUrl = orderItem.getProduct().getImage1Url();
            if (productImageUrl == null || productImageUrl.trim().isEmpty()) {
                productImageUrl = orderItem.getProduct().getThumbnail();
            }
        }
        return new OrderItemResponse(
            orderItem.getOrderItemId(),
            orderItem.getOrderId(),
            orderItem.getProductId(),
            orderItem.getPriceAtPurchase(),
            orderItem.getLicenseKey(),
            orderItem.getStatus(),
            orderItem.getCreated(),
            orderItem.getUpdated(),
            productName,
            productImageUrl
        );
    }
    
    @Override
    public PagedResponse<OrderResponse> getAllOrders(
            Integer tenantId, String username, boolean canAccessAllOrders, String status, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        boolean hasStatus = status != null && !status.trim().isEmpty();
        Page<Order> orderPage;

        if (!canAccessAllOrders) {
            String userId = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .map(u -> u.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
            orderPage = hasStatus
                    ? orderRepository.findPageByTenantIdAndUserIdAndStatus(tenantId, userId, status, pageable)
                    : orderRepository.findPageByTenantIdAndUserId(tenantId, userId, pageable);
        } else {
            orderPage = hasStatus
                    ? orderRepository.findPageByTenantIdAndStatus(tenantId, status, pageable)
                    : orderRepository.findPageByTenantId(tenantId, pageable);
        }

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PagedResponse.of(content, page, size, orderPage.getTotalElements());
    }

    @Override
    public OrderResponse getOrder(Integer tenantId, String orderId) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToDTO(order);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderResponse createOrder(Integer tenantId, String username, OrderRequest orderRequest) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        userRepository.findByTenantIdAndUserId(tenantId, orderRequest.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + orderRequest.getUserId()));

        String newOrderId = idGeneratorService.generateId(tenantId, "ORDER");
        BigDecimal finalAmount = orderRequest.getTotalAmount();
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (orderRequest.getDiscountCode() != null && !orderRequest.getDiscountCode().trim().isEmpty()) {
            try {
                discountAmount = discountService.applyDiscountToOrder(
                    tenantId,
                    orderRequest.getDiscountCode().trim(),
                    newOrderId,
                    orderRequest.getUserId(),
                    orderRequest.getTotalAmount(),
                    username
                );
                finalAmount = orderRequest.getTotalAmount().subtract(discountAmount);
            } catch (Exception e) {
                throw new BusinessException("Failed to apply discount code: " + e.getMessage());
            }
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            productRepository.findByTenantIdAndProductId(tenantId, itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId()));

            BigDecimal itemPrice = itemRequest.getPriceAtPurchase();

            OrderItem orderItem = new OrderItem();
            orderItem.setTenantId(tenantId);
            orderItem.setOrderId(newOrderId);
            orderItem.setOrderItemId(idGeneratorService.generateId(tenantId, "ORDER_ITEM"));
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setPriceAtPurchase(itemPrice);
            orderItem.setLicenseKey(itemRequest.getLicenseKey());
            orderItem.setStatus("0");
            orderItem.setCreatedBy(userService.getAuditCode(username));
            orderItem.setUpdatedBy(userService.getAuditCode(username));
            orderItem.setCreated(LocalDateTime.now());
            orderItem.setUpdated(LocalDateTime.now());
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setTenantId(tenantId);
        order.setOrderId(newOrderId);
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setCurrency(orderRequest.getCurrency());
        order.setTotalAmount(finalAmount);
        order.setExchangeRate(orderRequest.getExchangeRate());
        order.setStatus(OrderStatus.PENDING.getDisplayName());
        order.setCreatedBy(userService.getAuditCode(username));
        order.setUpdatedBy(userService.getAuditCode(username));

        for (OrderItem item : orderItems) {
            order.addOrderItem(item);
        }

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderStatusChangeEvent(savedOrder, null, savedOrder.getStatus()));

        return mapToDTO(savedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse updateOrder(Integer tenantId, String orderId, String username, OrderUpdateRequest updateRequest) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        String oldStatus = order.getStatus();

        if (updateRequest.getStatus() != null) {
            OrderStatus newStatus;
            try {
                newStatus = OrderStatus.fromDisplayName(updateRequest.getStatus());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid order status: " + updateRequest.getStatus());
            }

            if (!isValidStatusTransition(order.getStatus(), updateRequest.getStatus())) {
                throw new BusinessException("Invalid status transition from '" + order.getStatus() + "' to '" + updateRequest.getStatus() + "'");
            }

            order.setStatus(updateRequest.getStatus());
        }

        order.setUpdatedBy(userService.truncateUsernameForAudit(username));
        order.setUpdated(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        if (!oldStatus.equals(updatedOrder.getStatus())) {
            eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        }

        return mapToDTO(updatedOrder);
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) return true;

        String pending         = OrderStatus.PENDING.getDisplayName();
        String processing      = OrderStatus.PROCESSING.getDisplayName();
        String completed       = OrderStatus.COMPLETED.getDisplayName();
        String cancelled       = OrderStatus.CANCELLED.getDisplayName();
        String refunded        = OrderStatus.REFUNDED.getDisplayName();
        String partialRefunded = OrderStatus.PARTIALLY_REFUNDED.getDisplayName();

        if (currentStatus.equals(pending))          return List.of(processing, cancelled).contains(newStatus);
        if (currentStatus.equals(processing))       return List.of(completed, cancelled, refunded).contains(newStatus);
        if (currentStatus.equals(completed))        return List.of(refunded, partialRefunded).contains(newStatus);
        if (currentStatus.equals(partialRefunded))  return refunded.equals(newStatus);
        return false;
    }
    
    @Override
    @Transactional
    public void deleteOrder(Integer tenantId, String orderId) {
        orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        orderRepository.deleteByTenantIdAndOrderId(tenantId, orderId);
    }


    @Override
    @Transactional
    public OrderResponse completeOrder(Integer tenantId, String orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (OrderStatus.CANCELLED.getDisplayName().equals(order.getStatus()) || 
            OrderStatus.REFUNDED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Cannot complete order that is cancelled or refunded");
        }
        
        String oldStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED.getDisplayName());
        
        order.setUpdatedBy(userService.truncateUsernameForAudit(username));
        order.setUpdated(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));

        return mapToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Integer tenantId, String orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (OrderStatus.COMPLETED.getDisplayName().equals(order.getStatus()) || 
            OrderStatus.REFUNDED.getDisplayName().equals(order.getStatus()) ||
            OrderStatus.PARTIALLY_REFUNDED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Cannot cancel order that is completed or refunded");
        }
        
        String oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED.getDisplayName());
        
        order.setUpdatedBy(userService.truncateUsernameForAudit(username));
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse refundOrder(Integer tenantId, String orderId, String username) {
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (!OrderStatus.COMPLETED.getDisplayName().equals(order.getStatus())) {
            throw new BusinessException("Only completed orders can be refunded");
        }
        
        String oldStatus = order.getStatus();
        order.setStatus(OrderStatus.REFUNDED.getDisplayName());
        
        order.setUpdatedBy(userService.truncateUsernameForAudit(username));
        order.setUpdated(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        eventPublisher.publishEvent(new OrderStatusChangeEvent(updatedOrder, oldStatus, updatedOrder.getStatus()));
        
        return mapToDTO(updatedOrder);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPurchasedProduct(Integer tenantId, String userId, String productId) {
        return orderRepository.hasUserPurchasedProduct(tenantId, userId, productId);
    }
}