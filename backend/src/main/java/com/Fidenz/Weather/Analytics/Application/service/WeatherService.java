package com.Fidenz.Weather.Analytics.Application.service;

import com.Fidenz.Weather.Analytics.Application.dto.OpenWeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeatherService {

    private final RestClient restClient;
    private final String apiKey;

    public WeatherService(
            @Value("${openweathermap.base-url}") String baseUrl,
            @Value("${openweathermap.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

   
    @Cacheable(cacheNames = "weatherRaw", key = "#cityCode")
    public OpenWeatherResponse getWeatherForCity(long cityCode) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("id", cityCode)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .body(OpenWeatherResponse.class);
    }
}
