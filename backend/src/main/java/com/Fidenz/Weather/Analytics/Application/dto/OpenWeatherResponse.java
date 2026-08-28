package com.Fidenz.Weather.Analytics.Application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherResponse(
        long id,
        String name,
        List<WeatherDescription> weather,
        Main main,
        Wind wind,
        Clouds clouds,
        Integer visibility
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherDescription(String main, String description, String icon) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(
            double temp,
            double feels_like,
            double temp_min,
            double temp_max,
            int pressure,
            int humidity
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(double speed, Integer deg) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Clouds(int all) {}
}
