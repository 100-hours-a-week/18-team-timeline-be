package com.tamnara.backend.global.jwt;

import com.tamnara.backend.global.constant.JwtConstant;
import com.tamnara.backend.user.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKeyString;
    private Key secretKey;

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().toString())
                .claim("username", user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JwtConstant.ACCESS_TOKEN_VALIDITY))
                .signWith(secretKey)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String resolveTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (var cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
