package com.splashcan.backend.order;

import com.splashcan.backend.order.dto.CreateOrderRequest;
import com.splashcan.backend.order.dto.OrderResponse;
import com.splashcan.backend.order.dto.PayOrderRequest;
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
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(Authentication authentication,
                                                      @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(authentication.getName(), request));
    }

    @GetMapping
    public List<OrderResponse> getOrders(Authentication authentication) {
        return orderService.findOrders(authentication.getName());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(Authentication authentication, @PathVariable Long id) {
        return orderService.findOrder(authentication.getName(), id);
    }

    @PostMapping("/{id}/pay")
    public OrderResponse pay(Authentication authentication, @PathVariable Long id,
                              @RequestBody(required = false) PayOrderRequest request) {
        return orderService.pay(authentication.getName(), id, request);
    }
}
