package com.splashcan.backend.product.dto;

import com.splashcan.backend.product.ProductVariant;

import java.math.BigDecimal;

public record ProductVariantResponse(
        Long id,
        String flavor,
        Integer sizeMl,
        BigDecimal price,
        Integer stockQuantity,
        String sku
) {
    public static ProductVariantResponse from(ProductVariant variant) {
        return new ProductVariantResponse(
                variant.getId(),
                variant.getFlavor(),
                variant.getSizeMl(),
                variant.getPrice(),
                variant.getStockQuantity(),
                variant.getSku()
        );
    }
}
