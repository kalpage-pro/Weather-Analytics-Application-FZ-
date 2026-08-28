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

    /**
     * 0 inside the ideal band [18C, 24C]. Outside it, grows toward 1 as you move
     * further away, reaching 1.0 at +-15C from the nearest edge of the band.
     * Symmetric: extreme cold and extreme heat are penalized equally hard.
     */
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

    /**
     * Humidity mostly hurts comfort when it's already warm ("mugginess").
     * Above 20C: penalty ramps up between 60% and 100% relative humidity.
     * At or below 20C: humidity barely matters, so we apply a much smaller,
     * damp-chill effect only above 80%.
     */
    private double humidityPenalty(int humidity, double tempC) {
        if (tempC > 20.0) {
            return clamp((humidity - 60) / 40.0, 0.0, 1.0);
        }
        return 0.3 * clamp((humidity - 80) / 20.0, 0.0, 1.0);
    }

    /**
     * Wind's effect flips sign depending on temperature:
     *  - Cold (<10C): wind chill makes things worse -> penalty grows with speed.
     *  - Hot (>26C): a breeze helps -> small *negative* penalty (a bonus), capped.
     *  - Moderate (10-26C): only very strong wind (>8 m/s) is mildly penalized.
     */
    private double windPenalty(double windSpeed, double tempC) {
        if (tempC < 10.0) {
            return clamp(windSpeed / 15.0, 0.0, 1.0);
        }
        if (tempC > 26.0) {
            return -0.3 * clamp(windSpeed / 10.0, 0.0, 1.0);
        }
        return 0.3 * clamp((windSpeed - 8) / 12.0, 0.0, 1.0);
    }

    /**
     * Minor modifier: comfort dips slightly at both extremes of cloud cover -
     * fully clear (harsh sun/glare, and often hotter) and fully overcast (gloomy).
     * A partly-cloudy ~50% sky is treated as neutral.
     */
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
