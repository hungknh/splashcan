package com.splashcan.backend.order.dto;

import com.splashcan.backend.order.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long variantId,
        String flavor,
        Integer sizeMl,
        String sku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItem item) {
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new OrderItemResponse(
                item.getVariant().getId(),
                item.getVariant().getFlavor(),
                item.getVariant().getSizeMl(),
                item.getVariant().getSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                subtotal
        );
    }
}
