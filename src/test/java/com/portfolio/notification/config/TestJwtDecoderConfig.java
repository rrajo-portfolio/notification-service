package com.portfolio.notification.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestJwtDecoderConfig {

    @Bean
    @Primary
    JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
            .header("alg", "none")
            .claim("sub", "test-user")
            .build();
    }
}
