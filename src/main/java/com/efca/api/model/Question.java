package com.efca.api.model;

public record Question(String id, Domain domain, String text, boolean reverseScored) {

    public Question(String id, Domain domain, String text) {
        this(id, domain, text, false);
    }
}
