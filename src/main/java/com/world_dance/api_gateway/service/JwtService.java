package com.world_dance.api_gateway.service;

import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private SecretKey getSignKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token) {

        try {
            Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }

    public Long extractUserId(String token) {
        return extractClaim(
            token,
            claims -> claims.get("userId", Long.class)
        );
    }

    public String extractEmail(String token) {
        return extractClaim(
            token,
            Claims::getSubject
        );
    }
}