package com.tamnara.backend.user.controller;

import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.SignupRequestDto;
import com.tamnara.backend.user.dto.UserUpdateRequestDto;
import com.tamnara.backend.user.exception.DuplicateUsernameException;
import com.tamnara.backend.user.exception.UserNotFoundException;
import com.tamnara.backend.user.security.UserDetailsImpl;
import com.tamnara.backend.user.service.UserService;
import com.tamnara.backend.global.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PatchMapping("/me")
    public ResponseEntity<?> updateUsername(@RequestBody @Valid UserUpdateRequestDto dto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Long userId = userDetails.getUser().getId();
            User updatedUser = userService.updateUsername(userId, dto.getUsername());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원 정보가 성공적으로 수정되었습니다.",
                    "data", Map.of("user", Map.of("userId", updatedUser.getId()))
            ));

        } catch (DuplicateUsernameException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "message", "이미 사용 중인 닉네임입니다."
            ));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "관련된 회원이 없습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "서버 내부 에러가 발생했습니다. 나중에 다시 시도해주세요."
            ));
        }
    }

    @PatchMapping("/me/test")
    public ResponseEntity<?> updateUsernameForTest(@RequestParam("username") String username,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Long userId = userDetails.getUser().getId();
            User updatedUser = userService.updateUsername(userId, username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원 정보가 성공적으로 수정되었습니다.",
                    "data", Map.of("user", Map.of("userId", updatedUser.getId()))
            ));

        } catch (DuplicateUsernameException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "message", "이미 사용 중인 닉네임입니다."
            ));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "관련된 회원이 없습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "서버 내부 에러가 발생했습니다. 나중에 다시 시도해주세요."
            ));
        }
    }

}
