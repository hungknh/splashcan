package com.splashcan.backend.admin;

import com.splashcan.backend.admin.dto.AdminOrderResponse;
import com.splashcan.backend.exception.InvalidOrderStatusTransitionException;
import com.splashcan.backend.exception.OrderNotFoundException;
import com.splashcan.backend.order.Order;
import com.splashcan.backend.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<AdminOrderResponse> findAllOrders() {
        return orderRepository.findAll().stream()
                .map(AdminOrderResponse::from)
                .toList();
    }

    @Transactional
    public AdminOrderResponse updateStatus(Long orderId, Order.Status newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusTransitionException(order.getStatus().name(), newStatus.name());
        }
        order.setStatus(newStatus);
        orderRepository.save(order);
        return AdminOrderResponse.from(order);
    }
}
