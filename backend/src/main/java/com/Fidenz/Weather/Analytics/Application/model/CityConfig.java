package com.Fidenz.Weather.Analytics.Application.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public record CityConfig(
        @JsonProperty("CityCode") String cityCode,
        @JsonProperty("CityName") String cityName,
        @JsonProperty("Temp") String sampleTemp,
        @JsonProperty("Status") String sampleStatus
) {
    public long cityCodeAsLong() {
        return Long.parseLong(cityCode.trim());
    }
}
