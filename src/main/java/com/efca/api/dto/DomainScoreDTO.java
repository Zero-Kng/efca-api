package com.efca.api.dto;

public record DomainScoreDTO(
    String domain,
    String domainLabel,
    int sum,
    int maxPossible,
    double average
) {
}
