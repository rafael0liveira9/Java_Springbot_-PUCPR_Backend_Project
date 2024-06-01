package com.example.springtest.midleware;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.security.Key;

@Configuration
public class JwtConfig {

    private final String secret = "JWT+1810+convertJWT+1810+convertJWT+1810+convertJWT+1810+convert";

    @Bean
    public Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(secret);
    }
}