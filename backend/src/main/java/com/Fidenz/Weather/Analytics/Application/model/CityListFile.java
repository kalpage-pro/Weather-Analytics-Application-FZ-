package com.Fidenz.Weather.Analytics.Application.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record CityListFile(
        @JsonProperty("List") List<CityConfig> list
) {
}
