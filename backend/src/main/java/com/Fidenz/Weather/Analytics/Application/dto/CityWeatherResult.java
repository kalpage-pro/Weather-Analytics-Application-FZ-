package com.Fidenz.Weather.Analytics.Application.dto;

public record CityWeatherResult(
        long cityCode,
        String cityName,
        String weatherDescription,
        double temperatureCelsius,
        int humidity,
        double windSpeedMs,
        int cloudinessPct,
        ComfortResult comfort,
        int rank
) {
}