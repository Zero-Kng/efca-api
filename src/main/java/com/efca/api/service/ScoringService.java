package com.efca.api.service;

import com.efca.api.dto.AnswerRequest;
import com.efca.api.dto.DomainScoreDTO;
import com.efca.api.dto.ScoreResponse;
import com.efca.api.exception.InvalidAnswersException;
import com.efca.api.model.Domain;
import com.efca.api.model.Question;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoringService {

    private static final int MIN_SCALE = 1;
    private static final int MAX_SCALE = 5;

    private final QuestionBank questionBank;

    public ScoringService(QuestionBank questionBank) {
        this.questionBank = questionBank;
    }

    public ScoreResponse score(AnswerRequest request) {
        validateContent(request.answers());

        Map<Domain, Integer> sumByDomain = new EnumMap<>(Domain.class);
        Map<Domain, Integer> countByDomain = new EnumMap<>(Domain.class);
        for (Domain d : Domain.values()) {
            sumByDomain.put(d, 0);
            countByDomain.put(d, 0);
        }

        for (Map.Entry<String, Integer> entry : request.answers().entrySet()) {
            Question question = questionBank.get(entry.getKey());
            Domain domain = question.domain();
            int rawValue = entry.getValue();
            int effectiveValue = question.reverseScored() ? (MAX_SCALE + MIN_SCALE - rawValue) : rawValue;
            sumByDomain.merge(domain, effectiveValue, Integer::sum);
            countByDomain.merge(domain, 1, Integer::sum);
        }

        List<DomainScoreDTO> result = new ArrayList<>();
        for (Domain d : Domain.values()) {
            int sum = sumByDomain.get(d);
            int count = countByDomain.get(d);
            int maxPossible = count * MAX_SCALE;
            double average = count == 0 ? 0.0 : (double) sum / count;
            result.add(new DomainScoreDTO(d.name(), d.getLabel(), sum, maxPossible, round1(average)));
        }

        return new ScoreResponse(result);
    }

    private void validateContent(Map<String, Integer> answers) {
        List<String> errors = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : answers.entrySet()) {
            String questionId = entry.getKey();
            Integer value = entry.getValue();

            if (!questionBank.exists(questionId)) {
                errors.add("id de pergunta desconhecido: '" + questionId + "'");
                continue;
            }
            if (value == null || value < MIN_SCALE || value > MAX_SCALE) {
                errors.add("nota inválida para '" + questionId + "': deve ser um inteiro entre "
                    + MIN_SCALE + " e " + MAX_SCALE);
            }
        }

        if (answers.size() < questionBank.size()) {
            long missing = questionBank.size() - answers.size();
            errors.add(missing + " pergunta(s) não foram respondidas");
        }

        if (!errors.isEmpty()) {
            throw new InvalidAnswersException(errors);
        }
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 1cbb7d1d45233d4757ce4dcf49b93b1ca8a135d4
