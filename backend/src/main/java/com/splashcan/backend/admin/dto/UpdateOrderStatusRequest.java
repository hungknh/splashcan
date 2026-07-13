package com.splashcan.backend.admin.dto;

import com.splashcan.backend.order.Order;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull Order.Status status
) {
}
