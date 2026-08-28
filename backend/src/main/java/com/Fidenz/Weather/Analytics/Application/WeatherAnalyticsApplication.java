package com.Fidenz.Weather.Analytics.Application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WeatherAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherAnalyticsApplication.class, args);
	}

}
