package com.splashcan.backend.cart.dto;

import com.splashcan.backend.cart.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long variantId,
        String flavor,
        Integer sizeMl,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
    public static CartItemResponse from(CartItem item) {
        BigDecimal unitPrice = item.getVariant().getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                item.getVariant().getId(),
                item.getVariant().getFlavor(),
                item.getVariant().getSizeMl(),
                item.getVariant().getSku(),
                unitPrice,
                item.getQuantity(),
                subtotal
        );
    }
}
