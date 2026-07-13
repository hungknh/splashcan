package com.splashcan.backend.order;

import com.splashcan.backend.order.dto.CreateOrderRequest;
import com.splashcan.backend.order.dto.OrderResponse;
import com.splashcan.backend.order.dto.PayOrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Authenticated user's orders and mock payment")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create an order from the current cart")
    @ApiResponse(responseCode = "201", description = "Order created")
    @ApiResponse(responseCode = "400", description = "Validation failed, cart empty, or insufficient stock")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(Authentication authentication,
                                                      @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(authentication.getName(), request));
    }

    @Operation(summary = "List the current user's orders")
    @ApiResponse(responseCode = "200", description = "Orders returned")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @GetMapping
    public List<OrderResponse> getOrders(Authentication authentication) {
        return orderService.findOrders(authentication.getName());
    }

    @Operation(summary = "Get a single order belonging to the current user")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{id}")
    public OrderResponse getOrder(Authentication authentication, @PathVariable Long id) {
        return orderService.findOrder(authentication.getName(), id);
    }

    @Operation(summary = "Pay for an order (mock payment)")
    @ApiResponse(responseCode = "200", description = "Order paid")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @PostMapping("/{id}/pay")
    public OrderResponse pay(Authentication authentication, @PathVariable Long id,
                              @RequestBody(required = false) PayOrderRequest request) {
        return orderService.pay(authentication.getName(), id, request);
    }
}
