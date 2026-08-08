package com.efca.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Map;

<<<<<<< HEAD
=======

>>>>>>> 1cbb7d1d45233d4757ce4dcf49b93b1ca8a135d4
public record AnswerRequest(
    @NotEmpty(message = "answers não pode estar vazio")
    @Size(max = 16, message = "answers tem mais entradas do que perguntas existentes")
    Map<String, Integer> answers
) {
}
