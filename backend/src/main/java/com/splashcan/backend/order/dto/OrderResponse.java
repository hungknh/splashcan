package com.splashcan.backend.order.dto;

import com.splashcan.backend.order.Order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Order.Status status,
        BigDecimal totalAmount,
        String shippingAddress,
        List<OrderItemResponse> items,
        OffsetDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt()
        );
    }
}
