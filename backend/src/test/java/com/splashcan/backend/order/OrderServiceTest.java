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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String EMAIL = "user@test.com";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderService orderService;

    private User sampleUser() {
        return User.builder().id(1L).email(EMAIL).role(User.Role.CUSTOMER).build();
    }

    private ProductVariant sampleVariant(long id, BigDecimal price, int stockQuantity) {
        return ProductVariant.builder()
                .id(id)
                .flavor("Mango")
                .sizeMl(500)
                .price(price)
                .stockQuantity(stockQuantity)
                .sku("SKU-" + id)
                .build();
    }

    private CartItem cartItem(ProductVariant variant, int quantity) {
        return CartItem.builder().id(variant.getId()).variant(variant).quantity(quantity).build();
    }

    private Order sampleOrder(User user, Order.Status status) {
        return Order.builder()
                .id(1L)
                .user(user)
                .status(status)
                .totalAmount(BigDecimal.TEN)
                .shippingAddress("123 Main St")
                .build();
    }

    @Test
    void createOrderDecrementsStockAndComputesTotalAcrossItems() {
        User user = sampleUser();
        ProductVariant variantA = sampleVariant(1L, BigDecimal.valueOf(10), 20);
        ProductVariant variantB = sampleVariant(2L, BigDecimal.valueOf(5), 10);
        Cart cart = Cart.builder().id(10L).user(user).build();
        cart.getItems().add(cartItem(variantA, 2)); // subtotal 20
        cart.getItems().add(cartItem(variantB, 3)); // subtotal 15
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variantA));
        when(productVariantRepository.findById(2L)).thenReturn(Optional.of(variantB));

        OrderResponse response = orderService.createOrder(EMAIL, new CreateOrderRequest("123 Main St"));

        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(35));
        assertThat(variantA.getStockQuantity()).isEqualTo(18);
        assertThat(variantB.getStockQuantity()).isEqualTo(7);
        verify(productVariantRepository).save(variantA);
        verify(productVariantRepository).save(variantB);
        verify(orderRepository).save(any(Order.class));
        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    void createOrderThrowsCartEmptyWhenCartMissing() {
        User user = sampleUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(EMAIL, new CreateOrderRequest("addr")))
                .isInstanceOf(CartEmptyException.class);
    }

    @Test
    void createOrderThrowsCartEmptyWhenCartHasNoItems() {
        User user = sampleUser();
        Cart cart = Cart.builder().id(10L).user(user).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrder(EMAIL, new CreateOrderRequest("addr")))
                .isInstanceOf(CartEmptyException.class);
    }

    @Test
    void createOrderThrowsInsufficientStockWhenVariantDeletedSinceAddToCart() {
        User user = sampleUser();
        ProductVariant variant = sampleVariant(1L, BigDecimal.TEN, 5);
        Cart cart = Cart.builder().id(10L).user(user).build();
        cart.getItems().add(cartItem(variant, 2));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productVariantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(EMAIL, new CreateOrderRequest("addr")))
                .isInstanceOf(InsufficientStockException.class);

        verify(orderRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void createOrderThrowsInsufficientStockWhenLaterItemExceedsStock() {
        User user = sampleUser();
        ProductVariant okVariant = sampleVariant(1L, BigDecimal.TEN, 20);
        ProductVariant shortVariant = sampleVariant(2L, BigDecimal.valueOf(5), 1);
        Cart cart = Cart.builder().id(10L).user(user).build();
        cart.getItems().add(cartItem(okVariant, 2));
        cart.getItems().add(cartItem(shortVariant, 5)); // exceeds stock of 1
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(okVariant));
        when(productVariantRepository.findById(2L)).thenReturn(Optional.of(shortVariant));

        assertThatThrownBy(() -> orderService.createOrder(EMAIL, new CreateOrderRequest("addr")))
                .isInstanceOf(InsufficientStockException.class);

        // The earlier item's stock decrement/save already happened in the mock world (no
        // rollback), so we don't assert on it - only that the exception fires before the
        // order/cart are ever persisted, matching the real code's loop structure.
        verify(orderRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void findOrdersReturnsMappedResponsesForResolvedUser() {
        User user = sampleUser();
        Order order = sampleOrder(user, Order.Status.PENDING);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(order));

        List<OrderResponse> responses = orderService.findOrders(EMAIL);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        verify(orderRepository).findByUserOrderByCreatedAtDesc(user);
    }

    @Test
    void findOrderReturnsResponseWhenFoundAndOwned() {
        User user = sampleUser();
        Order order = sampleOrder(user, Order.Status.PENDING);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.findOrder(EMAIL, 1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void findOrderThrowsOrderNotFoundWhenMissingOrNotOwned() {
        User user = sampleUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findOrder(EMAIL, 99L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void paySucceedsMarksOrderPaidAndSavesSuccessPayment() {
        User user = sampleUser();
        Order order = sampleOrder(user, Order.Status.PENDING);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.pay(EMAIL, 1L, new PayOrderRequest(true));

        assertThat(response.status()).isEqualTo(Order.Status.PAID);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(Payment.Status.SUCCESS);
        verify(orderRepository).save(order);
    }

    @Test
    void payDefaultsToSuccessWhenRequestIsNull() {
        User user = sampleUser();
        Order order = sampleOrder(user, Order.Status.PENDING);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.pay(EMAIL, 1L, null);

        assertThat(response.status()).isEqualTo(Order.Status.PAID);
        verify(orderRepository).save(order);
    }

    @Test
    void payFailureLeavesOrderStatusUnchangedAndDoesNotSaveOrder() {
        User user = sampleUser();
        Order order = sampleOrder(user, Order.Status.PENDING);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.pay(EMAIL, 1L, new PayOrderRequest(false));

        assertThat(response.status()).isEqualTo(Order.Status.PENDING);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(Payment.Status.FAILED);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void payThrowsOrderNotFoundWhenMissingOrNotOwned() {
        User user = sampleUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.pay(EMAIL, 99L, new PayOrderRequest(true)))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
