package edu.orgresource.c0.api;

public class C0ValidationException extends RuntimeException {
    public C0ValidationException() {
        super("Request validation failed.");
    }
}
