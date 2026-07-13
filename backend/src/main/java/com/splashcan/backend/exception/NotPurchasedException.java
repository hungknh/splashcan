package com.splashcan.backend.exception;

public class NotPurchasedException extends RuntimeException {

    public NotPurchasedException() {
        super("You must purchase this product before reviewing it");
    }
}
