package com.splashcan.backend.exception;

public class CartEmptyException extends RuntimeException {

    public CartEmptyException() {
        super("Cart is empty");
    }
}
