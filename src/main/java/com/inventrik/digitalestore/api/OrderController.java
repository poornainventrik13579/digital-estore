package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.OrderFormRequest;
import com.inventrik.digitalestore.dto.request.OrderRequest;
import com.inventrik.digitalestore.dto.request.OrderUpdateRequest;
import com.inventrik.digitalestore.dto.response.OrderResponse;
import com.inventrik.digitalestore.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;
    
    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(orderService.getAllOrders(tenantId));
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "Get an order by ID")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "Order ID", required = true) 
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(tenantId, orderId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
        summary = "Create a new order with a single product item", 
        description = "Creates a new order with a single product item. For multiple items, submit multiple orders."
    )
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Valid @ModelAttribute OrderFormRequest formRequest,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        // Convert form request to regular OrderRequest
        OrderRequest orderRequest = formRequest.toOrderRequest();
        
        OrderResponse createdOrder = orderService.createOrder(tenantId, username, orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Create a new order with multiple items (JSON)", 
        description = "Creates a new order with multiple items using JSON body"
    )
    public ResponseEntity<OrderResponse> createOrderJson(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Valid @RequestBody OrderRequest orderRequest,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        OrderResponse createdOrder = orderService.createOrder(tenantId, username, orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }
    
    @PutMapping(path = "/{orderId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Update an order")
    public ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "Order ID", required = true) 
            @PathVariable Long orderId,
            @Valid @ModelAttribute OrderUpdateRequest updateRequest,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        OrderResponse updatedOrder = orderService.updateOrder(tenantId, orderId, username, updateRequest);
        return ResponseEntity.ok(updatedOrder);
    }
    
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Delete an order")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "Order ID", required = true) 
            @PathVariable Long orderId) {
        orderService.deleteOrder(tenantId, orderId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "User ID", required = true) 
            @PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(tenantId, userId));
    }
    
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get orders by status",
        description = "Retrieves orders filtered by status. Valid status values: " +
                      "Pending, Processing, Completed, Cancelled, Refunded, Partially Refunded"
    )
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(
                description = "Order status", 
                required = true,
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                    allowableValues = {"Pending", "Processing", "Completed", "Cancelled", "Refunded", "Partially Refunded"},
                    example = "Pending"
                )
            ) 
            @PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(tenantId, status));
    }
    
    @PostMapping("/{orderId}/complete")
    @Operation(summary = "Complete an order")
    public ResponseEntity<OrderResponse> completeOrder(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "Order ID", required = true) 
            @PathVariable Long orderId,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        OrderResponse completedOrder = orderService.completeOrder(tenantId, orderId, username);
        return ResponseEntity.ok(completedOrder);
    }
    
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "Order ID", required = true) 
            @PathVariable Long orderId,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        OrderResponse cancelledOrder = orderService.cancelOrder(tenantId, orderId, username);
        return ResponseEntity.ok(cancelledOrder);
    }
    
    @PostMapping("/{orderId}/refund")
    @Operation(summary = "Refund an order")
    public ResponseEntity<OrderResponse> refundOrder(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId,
            @Parameter(description = "Order ID", required = true) 
            @PathVariable Long orderId,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        OrderResponse refundedOrder = orderService.refundOrder(tenantId, orderId, username);
        return ResponseEntity.ok(refundedOrder);
    }
}