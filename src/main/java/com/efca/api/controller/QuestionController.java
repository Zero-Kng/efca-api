package com.efca.api.controller;

import com.efca.api.dto.QuestionDTO;
import com.efca.api.service.QuestionBank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class QuestionController {

    private final QuestionBank questionBank;

    public QuestionController(QuestionBank questionBank) {
        this.questionBank = questionBank;
    }

    @GetMapping("/api/questions")
    public List<QuestionDTO> listQuestions() {
        return questionBank.all().stream()
            .map(q -> new QuestionDTO(q.id(), q.domain().name(), q.domain().getLabel(), q.text()))
            .toList();
    }
}
