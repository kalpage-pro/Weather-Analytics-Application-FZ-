package com.Fidenz.Weather.Analytics.Application.service;

import com.Fidenz.Weather.Analytics.Application.dto.ComfortResult;
import com.Fidenz.Weather.Analytics.Application.dto.OpenWeatherResponse;
import org.springframework.stereotype.Service;


@Service
public class ComfortIndexService {

    private static final double W_TEMP = 0.45;
    private static final double W_HUMIDITY = 0.25;
    private static final double W_WIND = 0.20;
    private static final double W_CLOUD = 0.10;

    // Ideal temperature band, in Celsius, where temperature contributes no penalty at all.
    private static final double IDEAL_TEMP_LOW = 18.0;
    private static final double IDEAL_TEMP_HIGH = 24.0;

    public ComfortResult compute(OpenWeatherResponse response) {
        double tempC = response.main().temp(); // service must request units=metric from OWM
        int humidity = response.main().humidity();
        double windSpeed = response.wind() != null ? response.wind().speed() : 0.0;
        int cloudiness = response.clouds() != null ? response.clouds().all() : 0;

        double tempPenalty = temperaturePenalty(tempC);
        double humidityPenalty = humidityPenalty(humidity, tempC);
        double windPenalty = windPenalty(windSpeed, tempC);
        double cloudPenalty = cloudinessPenalty(cloudiness);

        double weightedPenalty =
                W_TEMP * tempPenalty
                        + W_HUMIDITY * humidityPenalty
                        + W_WIND * windPenalty
                        + W_CLOUD * cloudPenalty;

        double score = 100.0 - (100.0 * weightedPenalty);
        score = clamp(score, 0.0, 100.0);

        return new ComfortResult(
                round1(score),
                categoryFor(score),
                round1(tempPenalty),
                round1(humidityPenalty),
                round1(windPenalty),
                round1(cloudPenalty)
        );
    }

   
    private double temperaturePenalty(double tempC) {
        double deviation;
        if (tempC < IDEAL_TEMP_LOW) {
            deviation = IDEAL_TEMP_LOW - tempC;
        } else if (tempC > IDEAL_TEMP_HIGH) {
            deviation = tempC - IDEAL_TEMP_HIGH;
        } else {
            return 0.0;
        }
        return clamp(deviation / 15.0, 0.0, 1.0);
    }


    private double humidityPenalty(int humidity, double tempC) {
        if (tempC > 20.0) {
            return clamp((humidity - 60) / 40.0, 0.0, 1.0);
        }
        return 0.3 * clamp((humidity - 80) / 20.0, 0.0, 1.0);
    }

  
    private double windPenalty(double windSpeed, double tempC) {
        if (tempC < 10.0) {
            return clamp(windSpeed / 15.0, 0.0, 1.0);
        }
        if (tempC > 26.0) {
            return -0.3 * clamp(windSpeed / 10.0, 0.0, 1.0);
        }
        return 0.3 * clamp((windSpeed - 8) / 12.0, 0.0, 1.0);
    }

  
    private double cloudinessPenalty(int cloudinessPct) {
        return 0.2 * (Math.abs(cloudinessPct - 50) / 50.0);
    }

    private String categoryFor(double score) {
        if (score >= 80) return "Very Comfortable";
        if (score >= 60) return "Comfortable";
        if (score >= 40) return "Moderate";
        return "Uncomfortable";
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}