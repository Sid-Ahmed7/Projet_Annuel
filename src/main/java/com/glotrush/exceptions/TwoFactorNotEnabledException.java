package com.glotrush.exceptions;

public class TwoFactorNotEnabledException extends RuntimeException {
    public TwoFactorNotEnabledException(String message) {
        super(message);
    }
}
