package com.efca.api;

import com.efca.api.model.Domain;
import com.efca.api.model.Question;
import com.efca.api.service.QuestionBank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionBankTest {

    private final QuestionBank bank = new QuestionBank();

    @Test
    void hasSixteenQuestionsWithUniqueIds() {
        assertEquals(16, bank.size());
        assertEquals(16, new HashSet<>(bank.all().stream().map(Question::id).toList()).size());
    }

    @Test
    void onlyQuestionNineIsReverseScored() {
        List<String> reverse = bank.all().stream().filter(Question::reverseScored).map(Question::id).toList();

        assertEquals(List.of("q9"), reverse);
    }

    @ParameterizedTest
    @CsvSource({
        "HEDONICO, 2",
        "HIPERFAGICO, 4",
        "EMOCIONAL, 3",
        "COMPULSIVO, 3",
        "DESORGANIZADO, 4"
    })
    void itemsPerDomainMatchTheInstrument(Domain domain, long expected) {
        assertEquals(expected, bank.all().stream().filter(q -> q.domain() == domain).count());
    }

    @Test
    void lookupIsExactAndCaseSensitive() {
        assertTrue(bank.exists("q1"));
        assertFalse(bank.exists("Q1"));
        assertFalse(bank.exists("q17"));
    }

    @Test
    void questionListCannotBeModified() {
        assertThrows(UnsupportedOperationException.class, () -> bank.all().clear());
    }
}
