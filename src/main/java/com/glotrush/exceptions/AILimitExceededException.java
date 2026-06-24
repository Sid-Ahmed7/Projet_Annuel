package com.glotrush.exceptions;

public class AILimitExceededException extends RuntimeException {
    public AILimitExceededException(String message) {
        super(message);
    }
}
