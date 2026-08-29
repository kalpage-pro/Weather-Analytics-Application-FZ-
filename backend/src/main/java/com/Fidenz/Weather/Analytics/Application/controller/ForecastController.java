package com.Fidenz.Weather.Analytics.Application.controller;

import com.Fidenz.Weather.Analytics.Application.dto.ForecastPoint;
import com.Fidenz.Weather.Analytics.Application.service.ForecastService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    /**
     * Temperature trend for a single city, for the frontend's chart.
     * Next ~24 hours at 3-hour intervals.
     */
    @GetMapping("/api/cities/{cityCode}/forecast")
    public List<ForecastPoint> getForecast(@PathVariable long cityCode) {
        return forecastService.getForecastForCity(cityCode);
    }
}
