package com.example.demo.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {

    // SECRET KEY - Must be at least 256 bits (32 characters) for HS256
    // In production, store this in environment variables!
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    // Token validity: 24 hours (you can adjust this)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours
    // private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours
    // private static final long EXPIRATION_TIME = 1000 * 60 * 30; // 30 minutes

    // Get the signing key
    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate token with just username
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    // Generate token with extra claims
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .claims(extraClaims)                        // Add extra claims if any
                .subject(username)                          // Set the subject (username)
                .issuedAt(new Date())                       // When token was issued
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // When token expires
                .signWith(getSigningKey())                  // Sign with secret key using HS256
                .compact();                                 // Build the token
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }



    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Use your SecretKey object here
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}