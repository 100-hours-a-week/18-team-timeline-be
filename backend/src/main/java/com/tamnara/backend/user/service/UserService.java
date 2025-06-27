package com.tamnara.backend.user.service;

import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.SignupRequest;
import com.tamnara.backend.user.dto.SignupResponse;
import com.tamnara.backend.user.dto.UserInfo;
import com.tamnara.backend.user.dto.UserWithdrawInfoWrapper;

public interface UserService {
    SignupResponse signup(SignupRequest requestDto);
    boolean isEmailAvailable(String email);
    boolean isUsernameAvailable(String username);
    UserInfo getCurrentUserInfo(Long userId);
    User updateUsername(Long userId, String newUsername);
    UserWithdrawInfoWrapper withdrawUser(Long userId);
}
