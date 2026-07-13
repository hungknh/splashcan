package com.splashcan.backend.exception;

public class InvalidOrderStatusTransitionException extends RuntimeException {

    public InvalidOrderStatusTransitionException(String from, String to) {
        super("Cannot transition order from " + from + " to " + to);
    }
}
