package com.splashcan.backend.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long variantId, int available) {
        super("Insufficient stock for variant " + variantId + " (available: " + available + ")");
    }
}
