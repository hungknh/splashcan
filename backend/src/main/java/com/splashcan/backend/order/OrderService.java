package com.splashcan.backend.order;

import com.splashcan.backend.cart.Cart;
import com.splashcan.backend.cart.CartItem;
import com.splashcan.backend.cart.CartRepository;
import com.splashcan.backend.exception.CartEmptyException;
import com.splashcan.backend.exception.InsufficientStockException;
import com.splashcan.backend.exception.OrderNotFoundException;
import com.splashcan.backend.order.dto.CreateOrderRequest;
import com.splashcan.backend.order.dto.OrderResponse;
import com.splashcan.backend.order.dto.PayOrderRequest;
import com.splashcan.backend.payment.Payment;
import com.splashcan.backend.payment.PaymentRepository;
import com.splashcan.backend.product.ProductVariant;
import com.splashcan.backend.product.ProductVariantRepository;
import com.splashcan.backend.user.User;
import com.splashcan.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseThrow(CartEmptyException::new);
        if (cart.getItems().isEmpty()) {
            throw new CartEmptyException();
        }

        Order order = Order.builder()
                .user(user)
                .status(Order.Status.PENDING)
                .shippingAddress(request.shippingAddress())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = productVariantRepository.findById(cartItem.getVariant().getId())
                    .orElseThrow(() -> new InsufficientStockException(cartItem.getVariant().getId(), 0));

            if (cartItem.getQuantity() > variant.getStockQuantity()) {
                throw new InsufficientStockException(variant.getId(), variant.getStockQuantity());
            }
            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(subtotal);

            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .variant(variant)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(variant.getPrice())
                    .build());
        }
        order.setTotalAmount(total);
        orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findOrder(String email, Long orderId) {
        Order order = findOwnedOrder(email, orderId);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse pay(String email, Long orderId, PayOrderRequest request) {
        Order order = findOwnedOrder(email, orderId);

        boolean success = request == null || request.simulateSuccess() == null || request.simulateSuccess();
        Payment payment = Payment.builder()
                .order(order)
                .provider("MOCK")
                .transactionRef("MOCK-" + System.currentTimeMillis())
                .status(success ? Payment.Status.SUCCESS : Payment.Status.FAILED)
                .paidAt(success ? OffsetDateTime.now() : null)
                .build();
        paymentRepository.save(payment);

        if (success) {
            order.setStatus(Order.Status.PAID);
            orderRepository.save(order);
        }

        return OrderResponse.from(order);
    }

    private Order findOwnedOrder(String email, Long orderId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
