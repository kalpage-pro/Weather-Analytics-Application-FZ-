package com.Fidenz.Weather.Analytics.Application.controller;

import com.Fidenz.Weather.Analytics.Application.dto.CityWeatherResult;
import com.Fidenz.Weather.Analytics.Application.service.WeatherAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WeatherController {

    private final WeatherAnalyticsService weatherAnalyticsService;

    public WeatherController(WeatherAnalyticsService weatherAnalyticsService) {
        this.weatherAnalyticsService = weatherAnalyticsService;
    }


    @GetMapping("/api/cities")
    public List<CityWeatherResult> getCities() {
        return weatherAnalyticsService.getRankedCities();
    }
}
