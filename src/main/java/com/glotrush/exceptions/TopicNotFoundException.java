package com.glotrush.exceptions;

public class TopicNotFoundException extends RuntimeException{
    public TopicNotFoundException(String message) {
        super(message);
    }
}

