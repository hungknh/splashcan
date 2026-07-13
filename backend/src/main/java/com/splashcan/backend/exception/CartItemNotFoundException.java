package com.splashcan.backend.exception;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long id) {
        super("Cart item not found: " + id);
    }
}
