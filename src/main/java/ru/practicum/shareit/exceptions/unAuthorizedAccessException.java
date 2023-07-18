package ru.practicum.shareit.exceptions;

public class unAuthorizedAccessException extends RuntimeException {
    public unAuthorizedAccessException(String message) {
        super(message);
    }
}