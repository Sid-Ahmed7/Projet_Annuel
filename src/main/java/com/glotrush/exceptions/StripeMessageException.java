package com.glotrush.exceptions;

public class StripeMessageException extends RuntimeException {

        public StripeMessageException(String message) {
            super(message);
        }

        public StripeMessageException(String message, Throwable cause) {
            super(message, cause);
        }
    
}
