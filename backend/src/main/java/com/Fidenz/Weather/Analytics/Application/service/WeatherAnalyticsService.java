package com.Fidenz.Weather.Analytics.Application.service;

import com.Fidenz.Weather.Analytics.Application.dto.CityWeatherResult;
import com.Fidenz.Weather.Analytics.Application.dto.ComfortResult;
import com.Fidenz.Weather.Analytics.Application.dto.OpenWeatherResponse;
import com.Fidenz.Weather.Analytics.Application.model.CityConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class WeatherAnalyticsService {

    private final CityConfigService cityConfigService;
    private final WeatherService weatherService;
    private final ComfortIndexService comfortIndexService;

    public WeatherAnalyticsService(
            CityConfigService cityConfigService,
            WeatherService weatherService,
            ComfortIndexService comfortIndexService
    ) {
        this.cityConfigService = cityConfigService;
        this.weatherService = weatherService;
        this.comfortIndexService = comfortIndexService;
    }

   
    @Cacheable(cacheNames = "comfortScores", key = "'all'")
    public List<CityWeatherResult> getRankedCities() {
        List<CityConfig> cities = cityConfigService.getCities();
        List<CityWeatherResult> unranked = new ArrayList<>();

        for (CityConfig city : cities) {
            long cityCode = city.cityCodeAsLong();
            OpenWeatherResponse weather = weatherService.getWeatherForCity(cityCode);
            ComfortResult comfort = comfortIndexService.compute(weather);

            String description = weather.weather() != null && !weather.weather().isEmpty()
                    ? weather.weather().get(0).description()
                    : "unknown";

            unranked.add(new CityWeatherResult(
                    cityCode,
                    city.cityName(),
                    description,
                    weather.main().temp(),
                    weather.main().humidity(),
                    weather.wind() != null ? weather.wind().speed() : 0.0,
                    weather.clouds() != null ? weather.clouds().all() : 0,
                    comfort,
                    0 // rank assigned below
            ));
        }

        unranked.sort(Comparator.comparingDouble((CityWeatherResult c) -> c.comfort().score()).reversed());

        List<CityWeatherResult> ranked = new ArrayList<>(unranked.size());
        for (int i = 0; i < unranked.size(); i++) {
            CityWeatherResult c = unranked.get(i);
            ranked.add(new CityWeatherResult(
                    c.cityCode(), c.cityName(), c.weatherDescription(),
                    c.temperatureCelsius(), c.humidity(), c.windSpeedMs(), c.cloudinessPct(),
                    c.comfort(), i + 1
            ));
        }
        return ranked;
    }
}
