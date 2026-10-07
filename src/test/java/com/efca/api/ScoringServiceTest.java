package com.efca.api;

import com.efca.api.dto.AnswerRequest;
import com.efca.api.dto.DomainScoreDTO;
import com.efca.api.dto.ScoreResponse;
import com.efca.api.exception.InvalidAnswersException;
import com.efca.api.service.QuestionBank;
import com.efca.api.service.ScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoringServiceTest {

    private ScoringService scoringService;
    private QuestionBank questionBank;

    @BeforeEach
    void setUp() {
        questionBank = new QuestionBank();
        scoringService = new ScoringService(questionBank);
    }

    private Map<String, Integer> allAnswered(int value) {
        Map<String, Integer> answers = new HashMap<>();
        questionBank.all().forEach(q -> answers.put(q.id(), value));
        return answers;
    }

    private DomainScoreDTO domain(ScoreResponse response, String code) {
        return response.domains().stream()
            .filter(d -> d.domain().equals(code))
            .findFirst()
            .orElseThrow();
    }

    private ScoreResponse score(Map<String, Integer> answers) {
        return scoringService.score(new AnswerRequest(answers));
    }

    private List<String> errorsOf(Map<String, Integer> answers) {
        InvalidAnswersException ex = assertThrows(InvalidAnswersException.class, () -> score(answers));
        return ex.getErrors();
    }

    @Test
    void neutralAnswersProduceAverageThreeInEveryDomain() {
        ScoreResponse response = score(allAnswered(3));

        assertEquals(5, response.domains().size());
        response.domains().forEach(d -> assertEquals(3.0, d.average()));
    }

    @Test
    void domainsAreReturnedInStableOrder() {
        List<String> codes = score(allAnswered(3)).domains().stream().map(DomainScoreDTO::domain).toList();

        assertEquals(List.of("HEDONICO", "HIPERFAGICO", "EMOCIONAL", "COMPULSIVO", "DESORGANIZADO"), codes);
    }

    @ParameterizedTest
    @CsvSource({
        "HEDONICO, Hedônico, 10",
        "HIPERFAGICO, Hiperfágico, 20",
        "EMOCIONAL, Emocional, 15",
        "COMPULSIVO, Compulsivo, 15",
        "DESORGANIZADO, Desorganizado, 20"
    })
    void maxPossibleAndLabelMatchTheInstrument(String code, String label, int maxPossible) {
        DomainScoreDTO d = domain(score(allAnswered(3)), code);

        assertEquals(label, d.domainLabel());
        assertEquals(maxPossible, d.maxPossible());
    }

    @Test
    void reverseItemTurnsFiveIntoOne() {
        Map<String, Integer> answers = allAnswered(3);
        answers.put("q9", 5);

        DomainScoreDTO d = domain(score(answers), "DESORGANIZADO");

        assertEquals(10, d.sum());
        assertEquals(2.5, d.average());
    }

    @Test
    void reverseItemTurnsOneIntoFive() {
        Map<String, Integer> answers = allAnswered(3);
        answers.put("q9", 1);

        DomainScoreDTO d = domain(score(answers), "DESORGANIZADO");

        assertEquals(14, d.sum());
        assertEquals(3.5, d.average());
    }

    @Test
    void reverseItemDoesNotLeakIntoOtherDomains() {
        Map<String, Integer> answers = allAnswered(3);
        answers.put("q9", 5);

        score(answers).domains().stream()
            .filter(d -> !d.domain().equals("DESORGANIZADO"))
            .forEach(d -> assertEquals(3.0, d.average()));
    }

    @Test
    void highestPossibleProfileReachesMaximumInEveryDomain() {
        Map<String, Integer> answers = allAnswered(5);
        answers.put("q9", 1);

        score(answers).domains().forEach(d -> {
            assertEquals(d.maxPossible(), d.sum());
            assertEquals(5.0, d.average());
        });
    }

    @Test
    void lowestPossibleProfileReachesMinimumInEveryDomain() {
        Map<String, Integer> answers = allAnswered(1);
        answers.put("q9", 5);

        score(answers).domains().forEach(d -> {
            assertEquals(d.maxPossible() / 5, d.sum());
            assertEquals(1.0, d.average());
        });
    }

    @Test
    void averageIsRoundedToOneDecimalPlace() {
        Map<String, Integer> answers = allAnswered(1);
        answers.put("q14", 2);

        DomainScoreDTO d = domain(score(answers), "HIPERFAGICO");

        assertEquals(5, d.sum());
        assertEquals(1.3, d.average());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    void acceptsScaleBoundaries(int value) {
        Map<String, Integer> answers = allAnswered(3);
        answers.put("q1", value);

        assertDoesNotThrow(() -> score(answers));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 99, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void rejectsValuesOutsideTheScale(int value) {
        Map<String, Integer> answers = allAnswered(3);
        answers.put("q1", value);

        assertEquals(List.of("nota inválida para 'q1': deve ser um inteiro entre 1 e 5"), errorsOf(answers));
    }

    @Test
    void rejectsNullValue() {
        Map<String, Integer> answers = allAnswered(3);
        answers.put("q1", null);

        assertEquals(List.of("nota inválida para 'q1': deve ser um inteiro entre 1 e 5"), errorsOf(answers));
    }

    @ParameterizedTest
    @ValueSource(strings = {"q0", "q17", "q99", "Q1", " q1", "", "<script>"})
    void rejectsUnknownQuestionIds(String id) {
        Map<String, Integer> answers = allAnswered(3);
        answers.put(id, 3);

        assertTrue(errorsOf(answers).contains("id de pergunta desconhecido: '" + id + "'"));
    }

    @Test
    void rejectsIncompleteAnswers() {
        Map<String, Integer> answers = allAnswered(3);
        answers.remove("q1");

        assertEquals(List.of("1 pergunta(s) não foram respondidas"), errorsOf(answers));
    }

    @Test
    void unknownIdDoesNotCountAsAnsweredQuestion() {
        Map<String, Integer> answers = allAnswered(3);
        answers.remove("q1");
        answers.remove("q2");
        answers.put("q99", 3);

        List<String> errors = errorsOf(answers);

        assertTrue(errors.contains("2 pergunta(s) não foram respondidas"));
    }

    @Test
    void accumulatesEveryProblemInASingleResponse() {
        Map<String, Integer> answers = allAnswered(3);
        answers.remove("q16");
        answers.put("q3", 7);
        answers.put("q99", 3);

        List<String> errors = errorsOf(answers);

        assertEquals(3, errors.size());
        assertTrue(errors.contains("id de pergunta desconhecido: 'q99'"));
        assertTrue(errors.contains("nota inválida para 'q3': deve ser um inteiro entre 1 e 5"));
        assertTrue(errors.contains("1 pergunta(s) não foram respondidas"));
    }
}
