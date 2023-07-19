package ru.practicum.shareit.exceptions;

public class RestrictedAccessException extends RuntimeException {
    public RestrictedAccessException(String message) {
        super(message);
    }

    public RestrictedAccessException() {
        super();
    }
}