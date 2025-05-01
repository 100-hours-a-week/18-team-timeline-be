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

//    @Operation(
//            summary = "회원가입 API",
//            description = "이메일, 비밀번호, 닉네임을 입력받아 회원가입을 진행합니다."
//    )
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
//            @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
//            @ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복"),
//            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
//    })
//    @PostMapping
//    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto requestDto) {
//        try {
//            return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(requestDto));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(ErrorResponse.of(false, "서버 내부 에러가 발생했습니다. 나중에 다시 시도해주세요."));
//        }
//    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateUsername(@RequestBody @Valid UserUpdateRequestDto dto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Long userId = userDetails.getUser().getId();
            User updatedUser = userService.updateUsername(userId, dto.getNickname());

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
