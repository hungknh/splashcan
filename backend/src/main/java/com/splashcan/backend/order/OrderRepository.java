package com.splashcan.backend.order;

import com.splashcan.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    Optional<Order> findByIdAndUser(Long id, User user);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN :statuses")
    BigDecimal sumTotalAmountByStatusIn(@Param("statuses") List<Order.Status> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<Order.Status> statuses);

    @Query(value = "SELECT CAST(created_at AS DATE) AS day, COUNT(*) AS order_count, "
            + "COALESCE(SUM(total_amount), 0) AS revenue FROM orders WHERE status IN (:statuses) "
            + "GROUP BY CAST(created_at AS DATE) ORDER BY day DESC", nativeQuery = true)
    List<Object[]> aggregateOrdersByDay(@Param("statuses") List<String> statuses);
}
