package com.glotrush.exceptions;

public class SchedulerQuartzException extends RuntimeException {
    public SchedulerQuartzException(String message, Throwable cause)  {
        super(message, cause);  
    }
}
