package com.extreme.gym.exception;

public class LoginRateLimitException extends RuntimeException {

    public LoginRateLimitException(String message) {
        super(message);
    }
}
