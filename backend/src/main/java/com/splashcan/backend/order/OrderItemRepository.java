package com.splashcan.backend.order;

import com.splashcan.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi "
            + "WHERE oi.order.user = :user AND oi.variant.product.id = :productId "
            + "AND oi.order.status IN :statuses")
    boolean existsPurchaseByUserAndProduct(@Param("user") User user, @Param("productId") Long productId,
                                            @Param("statuses") List<Order.Status> statuses);
}
