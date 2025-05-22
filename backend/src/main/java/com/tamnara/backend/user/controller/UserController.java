package com.tamnara.backend.user.controller;

import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.SignupRequestDto;
import com.tamnara.backend.user.dto.UserInfoDto;
import com.tamnara.backend.user.dto.UserUpdateRequestDto;
import com.tamnara.backend.user.exception.DuplicateUsernameException;
import com.tamnara.backend.user.exception.UserNotFoundException;
import com.tamnara.backend.user.security.UserDetailsImpl;
import com.tamnara.backend.user.service.UserService;
import com.tamnara.backend.global.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @GetMapping("/check-email")
    @Operation(
            summary = "이메일 중복 조회",
            description = "입력된 이메일이 이미 가입된 이메일인지 확인합니다. 중복이면 false, 사용 가능하면 true를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공. 사용 가능 여부는 data.available로 확인"),
            @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<WrappedDTO<Map<String, Boolean>>> checkEmail(@RequestParam("email") String email) {
        try {
            if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                return ResponseEntity.badRequest().body(
                        new WrappedDTO<>(false, "이메일 형식이 잘못되었습니다.", null)
                );
            }

            boolean available = userService.isEmailAvailable(email);
            return ResponseEntity.ok(
                    new WrappedDTO<>(true,
                            available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.",
                            Map.of("available", available))
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new WrappedDTO<>(false, "서버 내부 오류가 발생했습니다. 나중에 다시 시도해주세요.", null)
            );
        }
    }

    @GetMapping("/check-nickname")
    @Operation(
            summary = "닉네임 중복 조회",
            description = "입력된 닉네임이 이미 가입된 닉네임인지 확인합니다. 중복이면 false, 사용 가능하면 true를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공. 사용 가능 여부는 data.available로 확인"),
            @ApiResponse(responseCode = "400", description = "잘못된 닉네임 형식"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<WrappedDTO<Map<String, Boolean>>> checkNickname(@RequestParam("nickname") String nickname) {
        try {
            if (nickname == null || nickname.isBlank() || nickname.length() > 10) {
                return ResponseEntity.badRequest().body(
                        new WrappedDTO<>(false, "닉네임 형식이 잘못되었습니다.", null)
                );
            }

            boolean available = userService.isUsernameAvailable(nickname);
            return ResponseEntity.ok(
                    new WrappedDTO<>(true,
                            available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.",
                            Map.of("available", available))
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new WrappedDTO<>(false, "서버 내부 오류가 발생했습니다. 나중에 다시 시도해주세요.", null)
            );
        }
    }

    @GetMapping("/me")
    @Operation(
            summary = "회원 정보 조회",
            description = "로그인된 사용자의 회원 정보를 반환합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청하신 정보를 성공적으로 불러왔습니다."),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "관련된 회원이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 에러가 발생했습니다.")
    })
    public ResponseEntity<WrappedDTO<Object>> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new WrappedDTO<>(false, "로그인이 필요합니다.", null)
                );
            }

            Long userId = userDetails.getUser().getId();
            UserInfoDto data = userService.getCurrentUserInfo(userId);

            return ResponseEntity.ok(
                    new WrappedDTO<>(true, "요청하신 정보를 성공적으로 불러왔습니다.", data)
            );

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new WrappedDTO<>(false, "관련된 회원이 없습니다.", null)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new WrappedDTO<>(false, "서버 내부 에러가 발생했습니다. 나중에 다시 시도해주세요.", null)
            );
        }
    }

    @PatchMapping("/me")
    @Operation(
            summary = "회원 정보 수정",
            description = "로그인된 사용자의 이름을 수정합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임입니다."),
            @ApiResponse(responseCode = "404", description = "관련된 회원이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 에러가 발생했습니다.")
    })
    public ResponseEntity<WrappedDTO<Map<String, Object>>> updateUsername(
            @RequestBody @Valid UserUpdateRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            Long userId = userDetails.getUser().getId();
            User updatedUser = userService.updateUsername(userId, dto.getNickname());

            return ResponseEntity.ok(
                    new WrappedDTO<>(true, "회원 정보가 성공적으로 수정되었습니다.",
                            Map.of("user", Map.of("userId", updatedUser.getId())))
            );

        } catch (DuplicateUsernameException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new WrappedDTO<>(false, "이미 사용 중인 닉네임입니다.", null)
            );
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new WrappedDTO<>(false, "관련된 회원이 없습니다.", null)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new WrappedDTO<>(false, "서버 내부 에러가 발생했습니다. 나중에 다시 시도해주세요.", null)
            );
        }
    }

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "현재 로그인한 사용자의 access token을 무효화하여 로그아웃 처리합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상적으로 로그아웃되었습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "403", description = "유효하지 않은 계정입니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다.")
    })
    public ResponseEntity<WrappedDTO<Void>> logout(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new WrappedDTO<>(false, "유효하지 않은 계정입니다.", null)
                );
            }

            return ResponseEntity.ok(
                    new WrappedDTO<>(true, "정상적으로 로그아웃되었습니다.", null)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new WrappedDTO<>(false, "서버 내부 오류가 발생했습니다. 나중에 다시 시도해주세요.", null)
            );
        }
    }
}

