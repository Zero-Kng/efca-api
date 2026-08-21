package com.efca.api.dto;

import java.util.List;

public record ScoreResponse(List<DomainScoreDTO> domains) {
}
