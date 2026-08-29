package com.Fidenz.Weather.Analytics.Application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherForecastResponse(
        List<ForecastEntry> list
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastEntry(
            long dt,
            Main main,
            String dt_txt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(double temp) {}
}
