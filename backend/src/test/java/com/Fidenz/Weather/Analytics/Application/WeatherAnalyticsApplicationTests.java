package com.Fidenz.Weather.Analytics.Application;

import com.Fidenz.Weather.Analytics.Application.dto.ComfortResult;
import com.Fidenz.Weather.Analytics.Application.dto.OpenWeatherResponse;
import com.Fidenz.Weather.Analytics.Application.service.ComfortIndexService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComfortIndexServiceTest {

    private final ComfortIndexService service = new ComfortIndexService();

    private OpenWeatherResponse weather(double temp, int humidity, double wind, int clouds) {
        return new OpenWeatherResponse(
                1, "TestCity",
                List.of(new OpenWeatherResponse.WeatherDescription("Clear", "clear sky", "01d")),
                new OpenWeatherResponse.Main(temp, temp, temp, temp, 1013, humidity),
                new OpenWeatherResponse.Wind(wind, 180),
                new OpenWeatherResponse.Clouds(clouds),
                10000
        );
    }

    @Test
    void idealConditionsScoreVeryHigh() {
        // 21C (mid ideal band), moderate humidity, light wind, partly cloudy
        ComfortResult result = service.compute(weather(21, 45, 2, 50));
        assertTrue(result.score() >= 90, "Expected near-perfect score, got " + result.score());
        assertEquals("Very Comfortable", result.category());
    }

    @Test
    void hotAndHumidScoresLow() {
        ComfortResult result = service.compute(weather(38, 90, 1, 20));
        assertTrue(result.score() < 40, "Expected low score for hot+humid, got " + result.score());
        assertEquals("Uncomfortable", result.category());
    }

    @Test
    void coldAndWindyScoresLow() {
        ComfortResult result = service.compute(weather(-5, 50, 12, 80));
        assertTrue(result.score() < 50, "Expected low score for cold+windy, got " + result.score());
    }

    @Test
    void breezeImprovesHotWeatherComfort() {
        ComfortResult calm = service.compute(weather(30, 40, 0, 50));
        ComfortResult breezy = service.compute(weather(30, 40, 8, 50));
        assertTrue(breezy.score() > calm.score(),
                "Expected breeze to help comfort when hot: calm=" + calm.score() + " breezy=" + breezy.score());
    }

    @Test
    void scoreIsAlwaysWithinBounds() {
        ComfortResult extreme = service.compute(weather(50, 100, 30, 100));
        assertTrue(extreme.score() >= 0 && extreme.score() <= 100);
    }
}
