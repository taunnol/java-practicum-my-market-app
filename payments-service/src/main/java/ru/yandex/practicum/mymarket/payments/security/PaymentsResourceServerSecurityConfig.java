package ru.yandex.practicum.mymarket.payments.security;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@Profile("!test")
@EnableWebFluxSecurity
@EnableConfigurationProperties(PaymentsSecurityProperties.class)
public class PaymentsResourceServerSecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/api/payments/**").authenticated()
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            OAuth2ResourceServerProperties resourceServerProperties,
            PaymentsSecurityProperties securityProperties
    ) {
        String issuer = resourceServerProperties.getJwt().getIssuerUri();

        NimbusReactiveJwtDecoder decoder = ReactiveJwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> azpValidator = jwt -> {
            String allowed = securityProperties.allowedClientId();
            if (allowed == null || allowed.isBlank()) {
                return OAuth2TokenValidatorResult.success();
            }

            String azp = jwt.getClaimAsString("azp");
            if (allowed.equals(azp)) {
                return OAuth2TokenValidatorResult.success();
            }

            OAuth2Error err = new OAuth2Error("invalid_token", "Invalid authorized party", null);
            return OAuth2TokenValidatorResult.failure(err);
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, azpValidator));
        return decoder;
    }
}
