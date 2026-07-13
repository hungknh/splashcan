package com.splashcan.backend.admin;

import com.splashcan.backend.admin.dto.AdminOrderResponse;
import com.splashcan.backend.admin.dto.UpdateOrderStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin - Orders", description = "Admin-only order listing and status management")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "List all orders")
    @ApiResponse(responseCode = "200", description = "Orders returned")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin")
    @GetMapping
    public List<AdminOrderResponse> getOrders() {
        return adminOrderService.findAllOrders();
    }

    @Operation(summary = "Update an order's status")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "400", description = "Validation failed or invalid status transition")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @PatchMapping("/{id}/status")
    public AdminOrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return adminOrderService.updateStatus(id, request.status());
    }
}
