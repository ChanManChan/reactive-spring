package com.reactivespring.exception;

public class ReviewDataException extends RuntimeException {
    private final String message;

    public ReviewDataException(String s) {
        super(s);
        this.message = s;
    }
}
