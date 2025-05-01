package com.tamnara.backend.global.jwt;

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

    private final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 30; // 30분

    @PostConstruct
    protected void init() {
        // 시크릿 키를 바이트로 인코딩해서 Key 객체로 변환
        System.out.println("✅ JWT_SECRET_CODE from env: " + secretKeyString); // 임시 출력
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
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

    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
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
            // 만료, 서명 오류, 파싱 오류 등
            return false;
        }
    }
}
