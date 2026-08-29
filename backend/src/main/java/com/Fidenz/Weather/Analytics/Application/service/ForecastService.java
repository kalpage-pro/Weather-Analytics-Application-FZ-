package com.Fidenz.Weather.Analytics.Application.service;

import com.Fidenz.Weather.Analytics.Application.dto.ForecastPoint;
import com.Fidenz.Weather.Analytics.Application.dto.OpenWeatherForecastResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ForecastService {

    private final RestClient restClient;
    private final String apiKey;

    public ForecastService(
            @Value("${openweathermap.forecast-url}") String forecastUrl,
            @Value("${openweathermap.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder().baseUrl(forecastUrl).build();
        this.apiKey = apiKey;
    }

    @Cacheable(cacheNames = "forecastRaw", key = "#cityCode")
    public List<ForecastPoint> getForecastForCity(long cityCode) {
        OpenWeatherForecastResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("id", cityCode)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .body(OpenWeatherForecastResponse.class);

        if (response == null || response.list() == null) {
            return List.of();
        }

        
        return response.list().stream()
                .limit(8)
                .map(entry -> new ForecastPoint(entry.dt_txt(), entry.main().temp()))
                .toList();
    }
}
