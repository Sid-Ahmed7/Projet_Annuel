package com.glotrush.exceptions;

public class TwoFactorAlreadyEnabledException extends RuntimeException {
    public TwoFactorAlreadyEnabledException(String message) {
        super(message);
    }
}
