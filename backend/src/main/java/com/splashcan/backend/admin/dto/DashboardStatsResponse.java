package com.splashcan.backend.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatsResponse(
        BigDecimal totalRevenue,
        long totalOrders,
        List<DailyOrderCount> ordersByDay
) {
    public record DailyOrderCount(String date, long orderCount, BigDecimal revenue) {
    }
}
