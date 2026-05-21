package ru.practicum.moviehub.api;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("Ошибка валидации");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
