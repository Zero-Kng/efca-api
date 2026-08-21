package com.efca.api.service;

import com.efca.api.model.Domain;
import com.efca.api.model.Question;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class QuestionBank {

    private static final List<Question> QUESTIONS = List.of(
        new Question("q1", Domain.HIPERFAGICO, "Geralmente como até me sentir cheio(a), estufado(a)."),
        new Question("q2", Domain.EMOCIONAL, "Uso a comida como uma forma de escape para acalmar minhas emoções."),
        new Question("q3", Domain.HIPERFAGICO, "Geralmente repito o prato nas refeições."),
        new Question("q4", Domain.DESORGANIZADO, "Tenho o hábito constante de petiscar entre as refeições."),
        new Question("q5", Domain.COMPULSIVO, "Quando começo a comer algo de que gosto, não consigo parar até terminar."),
        new Question("q6", Domain.HIPERFAGICO, "Costumo comer mais de um prato nas refeições principais."),
        new Question("q7", Domain.EMOCIONAL, "Faço lanches entre as refeições por ansiedade, tédio, solidão, medo, raiva, tristeza e/ou cansaço."),
        new Question("q8", Domain.HEDONICO, "Sinto-me tentado(a) a comer ao ver ou sentir o cheiro de uma comida de que gosto, ou ao passar por um quiosque, padaria, pizzaria ou fast food."),
        new Question("q9", Domain.DESORGANIZADO, "Tomo café da manhã todos os dias.", true),
        new Question("q10", Domain.EMOCIONAL, "Como nos momentos em que estou entediado(a), ansioso(a), nervoso(a), triste, cansado(a), irritado(a) ou solitário(a)."),
        new Question("q11", Domain.DESORGANIZADO, "Pulo algumas — ou pelo menos uma — das refeições principais."),
        new Question("q12", Domain.HEDONICO, "Quando me deparo com uma comida de que gosto muito, acabo comendo mesmo sem sentir fome."),
        new Question("q13", Domain.COMPULSIVO, "Como uma grande quantidade de comida em pouco tempo."),
        new Question("q14", Domain.HIPERFAGICO, "Quando como algo de que gosto, finalizo toda a porção."),
        new Question("q15", Domain.COMPULSIVO, "Quando como algo de que gosto muito, como muito rápido."),
        new Question("q16", Domain.DESORGANIZADO, "Passo mais de 5 horas por dia sem comer.")
    );

    private static final Map<String, Question> BY_ID = QUESTIONS.stream()
        .collect(Collectors.toMap(Question::id, q -> q));

    public List<Question> all() {
        return QUESTIONS;
    }

    public boolean exists(String questionId) {
        return BY_ID.containsKey(questionId);
    }

    public Question get(String questionId) {
        return BY_ID.get(questionId);
    }

    public int size() {
        return QUESTIONS.size();
    }
}
