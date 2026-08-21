package com.efca.api.exception;

import java.util.List;

public class InvalidAnswersException extends RuntimeException {

    private final List<String> errors;

    public InvalidAnswersException(List<String> errors) {
        super("Respostas inválidas: " + String.join("; ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
