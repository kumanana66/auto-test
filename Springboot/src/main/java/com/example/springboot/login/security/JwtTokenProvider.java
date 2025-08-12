package com.example.springboot.login.security;


import com.example.springboot.login.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String SECRET_KEY = "your_jwt_secret_key_should_be_long_and_secure";
    private static final long EXPIRATION_TIME = 2; // 2小时

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Instant now = Instant.now();
        Instant expiry = now.plus(EXPIRATION_TIME, ChronoUnit.HOURS);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parse(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            System.err.println("无效的JWT令牌");
        } catch (ExpiredJwtException ex) {
            System.err.println("过期的JWT令牌");
        } catch (UnsupportedJwtException ex) {
            System.err.println("不支持的JWT令牌");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT令牌为空");
        }
        return false;
    }
}