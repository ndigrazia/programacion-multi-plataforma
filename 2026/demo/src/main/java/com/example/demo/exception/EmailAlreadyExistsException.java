package com.example.demo.exception;

public class EmailAlreadyExistsException extends Exception {

    public EmailAlreadyExistsException() {
        super();
    }

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
