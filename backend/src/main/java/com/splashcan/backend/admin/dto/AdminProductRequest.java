package com.splashcan.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AdminProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @PositiveOrZero BigDecimal basePrice,
        @NotNull Long categoryId,
        String thumbnailUrl,
        String model3dUrl,
        boolean active
) {
}
