package edu.orgresource.extended.api;

public class C1ValidationException extends RuntimeException {
    public C1ValidationException() {
        super("Request validation failed.");
    }
}
