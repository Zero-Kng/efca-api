package com.efca.api;

import com.efca.api.dto.AnswerRequest;
import com.efca.api.dto.ScoreResponse;
import com.efca.api.exception.InvalidAnswersException;
import com.efca.api.service.QuestionBank;
import com.efca.api.service.ScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoringServiceTest {

    private ScoringService scoringService;
    private QuestionBank questionBank;

    @BeforeEach
    void setUp() {
        questionBank = new QuestionBank();
        scoringService = new ScoringService(questionBank);
    }

    private Map<String, Integer> completeValidAnswers() {
        Map<String, Integer> answers = new HashMap<>();
        questionBank.all().forEach(q -> answers.put(q.id(), 3));
        return answers;
    }

    @Test
    void computesAverageCorrectlyWhenAllAnswered() {
        Map<String, Integer> answers = completeValidAnswers();
        ScoreResponse response = scoringService.score(new AnswerRequest(answers));

        assertEquals(5, response.domains().size());
        response.domains().forEach(d -> assertEquals(3.0, d.average()));
    }

    @Test
    void rejectsUnknownQuestionId() {
        Map<String, Integer> answers = completeValidAnswers();
        answers.remove("q1");
        answers.put("q999-nao-existe", 3);

        assertThrows(InvalidAnswersException.class,
            () -> scoringService.score(new AnswerRequest(answers)));
    }

    @Test
    void rejectsOutOfRangeValue() {
        Map<String, Integer> answers = completeValidAnswers();
        answers.put("q1", 99);

        assertThrows(InvalidAnswersException.class,
            () -> scoringService.score(new AnswerRequest(answers)));
    }

    @Test
    void rejectsIncompleteAnswers() {
        Map<String, Integer> answers = completeValidAnswers();
        answers.remove("q1");

        assertThrows(InvalidAnswersException.class,
            () -> scoringService.score(new AnswerRequest(answers)));
    }
}
