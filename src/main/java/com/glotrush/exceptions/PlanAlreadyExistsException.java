package com.glotrush.exceptions;

public class PlanAlreadyExistsException extends RuntimeException {
    public PlanAlreadyExistsException(String message) {
        super(message);
    }
}
