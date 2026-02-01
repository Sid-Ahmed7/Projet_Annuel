package com.glotrush.exceptions;

public class SubscriptionAlreadyExistException extends RuntimeException {
     public SubscriptionAlreadyExistException(String message) {
        super(message);
    }
}
