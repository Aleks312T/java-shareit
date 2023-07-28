package ru.practicum.shareit.exceptions;

public class IncorrectParameterException extends RuntimeException {
    public IncorrectParameterException(String message) {
        super(message);
    }

}