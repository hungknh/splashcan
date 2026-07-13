package com.splashcan.backend.admin;

import com.splashcan.backend.admin.dto.DashboardStatsResponse;
import com.splashcan.backend.order.Order;
import com.splashcan.backend.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final List<Order.Status> PAID_STATUSES =
            List.of(Order.Status.PAID, Order.Status.SHIPPED, Order.Status.COMPLETED);
    private static final List<String> PAID_STATUS_NAMES = List.of("PAID", "SHIPPED", "COMPLETED");

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatusIn(PAID_STATUSES);
        long totalOrders = orderRepository.countByStatusIn(PAID_STATUSES);

        List<DashboardStatsResponse.DailyOrderCount> dailyStats = orderRepository
                .aggregateOrdersByDay(PAID_STATUS_NAMES).stream()
                .map(row -> new DashboardStatsResponse.DailyOrderCount(
                        row[0].toString(),
                        ((Number) row[1]).longValue(),
                        (BigDecimal) row[2]))
                .toList();

        return new DashboardStatsResponse(totalRevenue, totalOrders, dailyStats);
    }
}
