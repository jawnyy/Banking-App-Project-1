package com.jonathanbanda.exception;

public class DuplicateCustomerException extends Exception{
    public DuplicateCustomerException(String message) {
        super(message);
    }

    public DuplicateCustomerException(String message, Throwable cause) {
        super(message, cause);
    }
}
