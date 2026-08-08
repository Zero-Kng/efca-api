package com.efca.api.exception;

import java.util.List;

<<<<<<< HEAD
=======

>>>>>>> 1cbb7d1d45233d4757ce4dcf49b93b1ca8a135d4
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
