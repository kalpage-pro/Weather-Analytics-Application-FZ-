package com.Fidenz.Weather.Analytics.Application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.Fidenz.Weather.Analytics.Application.model.CityConfig;
import com.Fidenz.Weather.Analytics.Application.model.CityListFile;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class CityConfigService {

    private static final int MIN_CITIES_REQUIRED = 10;

    private final ObjectMapper objectMapper;
    private List<CityConfig> cities;

    public CityConfigService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadCities() {
        try (InputStream is = new ClassPathResource("cities.json").getInputStream()) {
            CityListFile file = objectMapper.readValue(is, CityListFile.class);
            this.cities = file.list();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load cities.json from classpath", e);
        }

        if (cities == null || cities.size() < MIN_CITIES_REQUIRED) {
            int actual = cities == null ? 0 : cities.size();
            
            System.err.printf(
                    "WARNING: cities.json has only %d cities, assignment requires at least %d.%n",
                    actual, MIN_CITIES_REQUIRED
            );
        }
    }

    public List<CityConfig> getCities() {
        return cities;
    }
}
