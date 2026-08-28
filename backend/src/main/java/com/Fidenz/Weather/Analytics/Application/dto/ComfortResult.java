package com.Fidenz.Weather.Analytics.Application.dto;

public record ComfortResult(
        double score,        // 0-100, higher = more comfortable
        String category,     // e.g. "Very Comfortable", "Comfortable", "Moderate", "Uncomfortable"
        double tempPenalty,
        double humidityPenalty,
        double windPenalty,
        double cloudPenalty
) {
}