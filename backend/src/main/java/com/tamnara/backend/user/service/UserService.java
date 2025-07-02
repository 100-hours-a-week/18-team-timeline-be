package com.tamnara.backend.user.service;

import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.SignupRequest;
import com.tamnara.backend.user.dto.SignupResponse;
import com.tamnara.backend.user.dto.UserInfo;
import com.tamnara.backend.user.dto.UserWithdrawInfo;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    SignupResponse signup(SignupRequest requestDto);
    boolean isEmailAvailable(String email);
    UserInfo getCurrentUserInfo(Long userId);
    User updateUsername(Long userId, String newUsername);
    UserWithdrawInfo withdrawUser(Long userId, HttpServletResponse response);
    void logout(Long userId, HttpServletResponse response);
}
