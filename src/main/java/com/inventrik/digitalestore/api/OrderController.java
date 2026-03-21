package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.OrderFormRequest;
import com.inventrik.digitalestore.dto.request.OrderRequest;
import com.inventrik.digitalestore.dto.request.OrderUpdateRequest;
import com.inventrik.digitalestore.dto.response.OrderResponse;
import com.inventrik.digitalestore.dto.response.PagedResponse;
import com.inventrik.digitalestore.security.TenantSecurity;
import com.inventrik.digitalestore.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
@SecurityRequirement(name = "oauth2")
public class OrderController {

    private final OrderService orderService;
    private final TenantSecurity tenantSecurity;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get order history (paginated, latest first). Regular users see only their own orders.")
    public ResponseEntity<PagedResponse<OrderResponse>> getAllOrders(
            @Parameter(description = "Tenant ID", required = true) @PathVariable Integer tenantId,
            @Parameter(description = "Filter by order status") @RequestParam(required = false) String status,
            @Parameter(description = "Page number, 0-based") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);

        boolean canAccessAllOrders = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TENANT"));

        return ResponseEntity.ok(orderService.getAllOrders(
                tenantId, authentication.getName(), canAccessAllOrders, status, page, size));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get an order by ID")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Integer tenantId,
            @PathVariable String orderId,
            Authentication authentication) {
        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        return ResponseEntity.ok(orderService.getOrder(tenantId, orderId));
    }

    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Create order (form)")
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable Integer tenantId,
            @Valid @ModelAttribute OrderFormRequest formRequest,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(tenantId, authentication.getName(), formRequest.toOrderRequest()));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Create order (JSON)")
    public ResponseEntity<OrderResponse> createOrderJson(
            @PathVariable Integer tenantId,
            @Valid @RequestBody OrderRequest orderRequest,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(tenantId, authentication.getName(), orderRequest));
    }

    @PutMapping(path = "/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Update an order (JSON)")
    public ResponseEntity<OrderResponse> updateOrderJson(
            @PathVariable Integer tenantId,
            @PathVariable String orderId,
            @Valid @RequestBody OrderUpdateRequest updateRequest,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        return ResponseEntity.ok(
                orderService.updateOrder(tenantId, orderId, authentication.getName(), updateRequest));
    }

    @PutMapping(path = "/{orderId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Update an order (Form)")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Integer tenantId,
            @PathVariable String orderId,
            @Valid @ModelAttribute OrderUpdateRequest updateRequest,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        return ResponseEntity.ok(
                orderService.updateOrder(tenantId, orderId, authentication.getName(), updateRequest));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Delete an order")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Integer tenantId,
            @PathVariable String orderId) {
        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        orderService.deleteOrder(tenantId, orderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Complete an order")
    public ResponseEntity<OrderResponse> completeOrder(
            @PathVariable Integer tenantId,
            @PathVariable String orderId,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        return ResponseEntity.ok(orderService.completeOrder(tenantId, orderId, authentication.getName()));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Integer tenantId,
            @PathVariable String orderId,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        return ResponseEntity.ok(orderService.cancelOrder(tenantId, orderId, authentication.getName()));
    }

    @PostMapping("/{orderId}/refund")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Refund an order")
    public ResponseEntity<OrderResponse> refundOrder(
            @PathVariable Integer tenantId,
            @PathVariable String orderId,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        return ResponseEntity.ok(orderService.refundOrder(tenantId, orderId, authentication.getName()));
    }
}
