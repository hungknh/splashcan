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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public CartResponse getCart(String email) {
        return CartResponse.from(getOrCreateCart(email));
    }

    @Transactional
    public CartResponse addItem(String email, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(email);
        ProductVariant variant = productVariantRepository.findById(request.variantId())
                .orElseThrow(() -> new VariantNotFoundException(request.variantId()));

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> item.getVariant().getId().equals(variant.getId()))
                .findFirst();

        int newQuantity = request.quantity() + existing.map(CartItem::getQuantity).orElse(0);
        if (newQuantity > variant.getStockQuantity()) {
            throw new InsufficientStockException(variant.getId(), variant.getStockQuantity());
        }

        if (existing.isPresent()) {
            existing.get().setQuantity(newQuantity);
        } else {
            cart.getItems().add(CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.quantity())
                    .build());
        }
        cartRepository.save(cart);
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse updateItem(String email, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(email);
        CartItem item = findOwnedItem(cart, itemId);

        if (request.quantity() > item.getVariant().getStockQuantity()) {
            throw new InsufficientStockException(item.getVariant().getId(), item.getVariant().getStockQuantity());
        }
        item.setQuantity(request.quantity());
        cartRepository.save(cart);
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse removeItem(String email, Long itemId) {
        Cart cart = getOrCreateCart(email);
        CartItem item = findOwnedItem(cart, itemId);
        cart.getItems().remove(item);
        cartRepository.save(cart);
        return CartResponse.from(cart);
    }

    private CartItem findOwnedItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(itemId));
    }

    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }
}
