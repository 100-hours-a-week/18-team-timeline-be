package com.tamnara.backend.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    void refreshToken(HttpServletRequest request, HttpServletResponse response);
}
