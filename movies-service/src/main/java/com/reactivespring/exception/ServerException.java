package com.reactivespring.exception;

public class ServerException extends RuntimeException {
    private final String message;

    public ServerException(String message) {
        super(message);
        this.message = message;
    }
}
