package com.splashcan.backend.cart;

import com.splashcan.backend.cart.dto.AddCartItemRequest;
import com.splashcan.backend.cart.dto.CartResponse;
import com.splashcan.backend.cart.dto.UpdateCartItemRequest;
import com.splashcan.backend.exception.CartItemNotFoundException;
import com.splashcan.backend.exception.InsufficientStockException;
import com.splashcan.backend.exception.VariantNotFoundException;
import com.splashcan.backend.product.ProductVariant;
import com.splashcan.backend.product.ProductVariantRepository;
import com.splashcan.backend.user.User;
import com.splashcan.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String EMAIL = "user@test.com";

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private CartService cartService;

    private User sampleUser() {
        return User.builder().id(1L).email(EMAIL).role(User.Role.CUSTOMER).build();
    }

    private ProductVariant sampleVariant(long id, int stockQuantity) {
        return ProductVariant.builder()
                .id(id)
                .flavor("Mango")
                .sizeMl(500)
                .price(BigDecimal.valueOf(9.99))
                .stockQuantity(stockQuantity)
                .sku("SKU-" + id)
                .build();
    }

    /** Stubs the user/cart lookups so getOrCreateCart resolves to the given already-existing cart. */
    private void givenExistingCart(User user, Cart cart) {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
    }

    @Test
    void getCartCreatesNewCartWhenUserHasNone() {
        User user = sampleUser();
        Cart savedCart = Cart.builder().id(10L).user(user).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        CartResponse response = cartService.getCart(EMAIL);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.items()).isEmpty();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCartReturnsExistingCartWithoutSaving() {
        User user = sampleUser();
        Cart existingCart = Cart.builder().id(10L).user(user).build();
        givenExistingCart(user, existingCart);

        CartResponse response = cartService.getCart(EMAIL);

        assertThat(response.id()).isEqualTo(10L);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItemAddsNewCartItemWhenVariantNotInCart() {
        User user = sampleUser();
        Cart cart = Cart.builder().id(10L).user(user).build();
        ProductVariant variant = sampleVariant(5L, 20);
        givenExistingCart(user, cart);
        when(productVariantRepository.findById(5L)).thenReturn(Optional.of(variant));

        cartService.addItem(EMAIL, new AddCartItemRequest(5L, 3));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(cart.getItems().get(0).getVariant()).isEqualTo(variant);
        verify(cartRepository).save(cart);
    }

    @Test
    void addItemMergesQuantityWhenVariantAlreadyInCart() {
        User user = sampleUser();
        ProductVariant variant = sampleVariant(5L, 20);
        Cart cart = Cart.builder().id(10L).user(user).build();
        cart.getItems().add(CartItem.builder().id(1L).cart(cart).variant(variant).quantity(2).build());
        givenExistingCart(user, cart);
        when(productVariantRepository.findById(5L)).thenReturn(Optional.of(variant));

        cartService.addItem(EMAIL, new AddCartItemRequest(5L, 3));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        verify(cartRepository).save(cart);
    }

    @Test
    void addItemThrowsInsufficientStockWhenQuantityExceedsStock() {
        User user = sampleUser();
        ProductVariant variant = sampleVariant(5L, 2);
        Cart cart = Cart.builder().id(10L).user(user).build();
        givenExistingCart(user, cart);
        when(productVariantRepository.findById(5L)).thenReturn(Optional.of(variant));

        assertThatThrownBy(() -> cartService.addItem(EMAIL, new AddCartItemRequest(5L, 3)))
                .isInstanceOf(InsufficientStockException.class);

        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItemThrowsVariantNotFoundWhenVariantIdUnknown() {
        User user = sampleUser();
        Cart cart = Cart.builder().id(10L).user(user).build();
        givenExistingCart(user, cart);
        when(productVariantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(EMAIL, new AddCartItemRequest(99L, 1)))
                .isInstanceOf(VariantNotFoundException.class);
    }

    @Test
    void updateItemUpdatesQuantityAndSaves() {
        User user = sampleUser();
        ProductVariant variant = sampleVariant(5L, 20);
        Cart cart = Cart.builder().id(10L).user(user).build();
        CartItem item = CartItem.builder().id(1L).cart(cart).variant(variant).quantity(2).build();
        cart.getItems().add(item);
        givenExistingCart(user, cart);

        cartService.updateItem(EMAIL, 1L, new UpdateCartItemRequest(7));

        assertThat(item.getQuantity()).isEqualTo(7);
        verify(cartRepository).save(cart);
    }

    @Test
    void updateItemThrowsInsufficientStockWhenNewQuantityExceedsStock() {
        User user = sampleUser();
        ProductVariant variant = sampleVariant(5L, 5);
        Cart cart = Cart.builder().id(10L).user(user).build();
        cart.getItems().add(CartItem.builder().id(1L).cart(cart).variant(variant).quantity(2).build());
        givenExistingCart(user, cart);

        assertThatThrownBy(() -> cartService.updateItem(EMAIL, 1L, new UpdateCartItemRequest(10)))
                .isInstanceOf(InsufficientStockException.class);

        verify(cartRepository, never()).save(any());
    }

    @Test
    void updateItemThrowsCartItemNotFoundWhenItemIdUnknown() {
        User user = sampleUser();
        Cart cart = Cart.builder().id(10L).user(user).build();
        givenExistingCart(user, cart);

        assertThatThrownBy(() -> cartService.updateItem(EMAIL, 99L, new UpdateCartItemRequest(1)))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    @Test
    void removeItemRemovesItemAndSaves() {
        User user = sampleUser();
        ProductVariant variant = sampleVariant(5L, 20);
        Cart cart = Cart.builder().id(10L).user(user).build();
        CartItem item = CartItem.builder().id(1L).cart(cart).variant(variant).quantity(2).build();
        cart.getItems().add(item);
        givenExistingCart(user, cart);

        cartService.removeItem(EMAIL, 1L);

        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItemThrowsCartItemNotFoundWhenItemIdUnknown() {
        User user = sampleUser();
        Cart cart = Cart.builder().id(10L).user(user).build();
        givenExistingCart(user, cart);

        assertThatThrownBy(() -> cartService.removeItem(EMAIL, 99L))
                .isInstanceOf(CartItemNotFoundException.class);
    }
}
