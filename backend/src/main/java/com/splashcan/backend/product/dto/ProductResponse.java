package com.splashcan.backend.product.dto;

import com.splashcan.backend.category.dto.CategoryResponse;
import com.splashcan.backend.product.Product;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        CategoryResponse category,
        String thumbnailUrl,
        String model3dUrl,
        List<ProductVariantResponse> variants
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice(),
                CategoryResponse.from(product.getCategory()),
                product.getThumbnailUrl(),
                product.getModel3dUrl(),
                product.getVariants().stream().map(ProductVariantResponse::from).toList()
        );
    }
}
