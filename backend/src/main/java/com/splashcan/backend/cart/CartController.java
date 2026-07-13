package com.splashcan.backend.cart;

import com.splashcan.backend.cart.dto.AddCartItemRequest;
import com.splashcan.backend.cart.dto.CartResponse;
import com.splashcan.backend.cart.dto.UpdateCartItemRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        return cartService.getCart(authentication.getName());
    }

    @PostMapping("/items")
    public CartResponse addItem(Authentication authentication, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(authentication.getName(), request);
    }

    @PutMapping("/items/{id}")
    public CartResponse updateItem(Authentication authentication, @PathVariable Long id,
                                    @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(authentication.getName(), id, request);
    }

    @DeleteMapping("/items/{id}")
    public CartResponse removeItem(Authentication authentication, @PathVariable Long id) {
        return cartService.removeItem(authentication.getName(), id);
    }
}
