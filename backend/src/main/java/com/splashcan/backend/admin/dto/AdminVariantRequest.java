package com.splashcan.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AdminVariantRequest(
        @NotBlank String flavor,
        @NotNull @Positive Integer sizeMl,
        @NotNull @PositiveOrZero BigDecimal price,
        @NotNull @PositiveOrZero Integer stockQuantity,
        @NotBlank String sku
) {
}
