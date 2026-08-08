package com.efca.api.controller;

import com.efca.api.dto.AnswerRequest;
import com.efca.api.dto.ScoreResponse;
import com.efca.api.service.ScoringService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

<<<<<<< HEAD
=======

>>>>>>> 1cbb7d1d45233d4757ce4dcf49b93b1ca8a135d4
@RestController
public class ScoreController {

    private final ScoringService scoringService;

    public ScoreController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @PostMapping("/api/responses")
    public ScoreResponse submitAnswers(@Valid @RequestBody AnswerRequest request) {
        return scoringService.score(request);
    }
}
