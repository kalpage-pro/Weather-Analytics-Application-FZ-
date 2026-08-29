package com.Fidenz.Weather.Analytics.Application.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Custom validator: Spring Security ships no built-in audience check, so this
 * confirms the token's "aud" claim contains the expected Auth0 API identifier.
 * (This is the same pattern Auth0's own Spring Boot quickstart uses.)
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String expectedAudience;

    public AudienceValidator(String expectedAudience) {
        this.expectedAudience = expectedAudience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience().contains(expectedAudience)) {
            return OAuth2TokenValidatorResult.success();
        }
        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "The required audience " + expectedAudience + " is missing",
                null
        );
        return OAuth2TokenValidatorResult.failure(error);
    }
}
