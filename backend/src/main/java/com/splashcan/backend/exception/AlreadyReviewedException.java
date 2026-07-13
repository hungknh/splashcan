package com.splashcan.backend.exception;

public class AlreadyReviewedException extends RuntimeException {

    public AlreadyReviewedException() {
        super("You have already reviewed this product");
    }
}
