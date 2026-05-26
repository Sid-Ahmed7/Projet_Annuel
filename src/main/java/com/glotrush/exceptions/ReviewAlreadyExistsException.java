package com.glotrush.exceptions;

public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(String message) {
        super(message);
    }
    
}
