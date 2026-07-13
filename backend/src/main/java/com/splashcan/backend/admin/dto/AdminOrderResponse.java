package com.splashcan.backend.admin.dto;

import com.splashcan.backend.order.Order;
import com.splashcan.backend.order.dto.OrderItemResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AdminOrderResponse(
        Long id,
        String userEmail,
        Order.Status status,
        BigDecimal totalAmount,
        String shippingAddress,
        List<OrderItemResponse> items,
        OffsetDateTime createdAt
) {
    public static AdminOrderResponse from(Order order) {
        return new AdminOrderResponse(
                order.getId(),
                order.getUser().getEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt()
        );
    }
}
