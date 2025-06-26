package com.tamnara.backend.global.security;

import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.user.security.UserDetailsImpl;
import com.tamnara.backend.user.security.UserDetailsServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/health");
    } //헬스체크


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = jwtProvider.resolveAccessTokenFromCookie(request);
            if (token != null && jwtProvider.validateAccessToken(token)) {
                String userId = jwtProvider.getUserIdFromToken(token);
                UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(userId);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException e) {
            log.warn("[WARN] JWT 예외 발생: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
