package com.efca.api.model;

public enum Domain {
    HEDONICO("Hedônico"),
    HIPERFAGICO("Hiperfágico"),
    EMOCIONAL("Emocional"),
    COMPULSIVO("Compulsivo"),
    DESORGANIZADO("Desorganizado");

    private final String label;

    Domain(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
