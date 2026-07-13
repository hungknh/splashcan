package com.splashcan.backend.admin;

import com.splashcan.backend.admin.dto.DashboardStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin - Dashboard", description = "Admin-only summary statistics")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "Get dashboard summary statistics")
    @ApiResponse(responseCode = "200", description = "Statistics returned")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin")
    @GetMapping("/stats")
    public DashboardStatsResponse getStats() {
        return adminDashboardService.getStats();
    }
}
