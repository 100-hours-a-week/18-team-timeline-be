package com.tamnara.backend.user.controller;

import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.user.constant.UserResponseMessage;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.EmailAvailabilityResponse;
import com.tamnara.backend.user.dto.UserInfo;
import com.tamnara.backend.user.dto.UserInfoWrapper;
import com.tamnara.backend.user.dto.UserUpdateRequest;
import com.tamnara.backend.user.dto.UserUpdateResponse;
import com.tamnara.backend.user.dto.UserWithdrawInfoWrapper;
import com.tamnara.backend.user.exception.DuplicateUsernameException;
import com.tamnara.backend.user.exception.InactiveUserException;
import com.tamnara.backend.user.exception.UserNotFoundException;
import com.tamnara.backend.user.security.UserDetailsImpl;
import com.tamnara.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<WrappedDTO<EmailAvailabilityResponse>> checkEmail(@RequestParam("email") String email) {
        try {
            boolean available = userService.isEmailAvailable(email);
            return ResponseEntity.ok(new WrappedDTO<>(
                    true,
                    available ? UserResponseMessage.EMAIL_AVAILABLE : UserResponseMessage.EMAIL_UNAVAILABLE,
                    new EmailAvailabilityResponse(available)
            ));

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
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
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "403", description = "유효하지 않은 계정입니다."),
            @ApiResponse(responseCode = "404", description = "관련된 회원이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 에러가 발생했습니다.")
    })
    public ResponseEntity<WrappedDTO<UserInfoWrapper>> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            UserInfo userInfo = userService.getCurrentUserInfo(userDetails.getUser().getId());
            UserInfoWrapper data = new UserInfoWrapper(userInfo);

            return ResponseEntity.ok(new WrappedDTO<>(
                    true,
                    UserResponseMessage.USER_INFO_RETRIEVED,
                    data
            ));

        } catch (InactiveUserException e) {
            throw new CustomException(HttpStatus.FORBIDDEN, ResponseMessage.USER_FORBIDDEN);
        } catch (UserNotFoundException e) {
            throw new CustomException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
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
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "403", description = "유효하지 않은 계정입니다."),
            @ApiResponse(responseCode = "404", description = "관련된 회원이 없습니다."),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임입니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 에러가 발생했습니다.")
    })
    public ResponseEntity<WrappedDTO<UserUpdateResponse>> updateUsername(
            @RequestBody @Valid UserUpdateRequest dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User updatedUser = userService.updateUsername(userDetails.getUser().getId(), dto.getNickname());
            return ResponseEntity.ok(new WrappedDTO<>(
                    true,
                    UserResponseMessage.USER_INFO_MODIFIED,
                    new UserUpdateResponse(updatedUser.getId())
            ));

        } catch (DuplicateUsernameException e) {
            throw new CustomException(HttpStatus.CONFLICT, UserResponseMessage.NICKNAME_UNAVAILABLE);
        } catch (InactiveUserException e) {
            throw new CustomException(HttpStatus.FORBIDDEN, ResponseMessage.USER_FORBIDDEN);
        } catch (UserNotFoundException e) {
            throw new CustomException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
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
            @ApiResponse(responseCode = "404", description = "관련된 회원이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다.")
    })
    public ResponseEntity<WrappedDTO<Void>> logout(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletResponse response
    ) {
        try {
            userService.logout(userDetails.getUser().getId(), response);
            return ResponseEntity.ok(new WrappedDTO<>(
                    true,
                    UserResponseMessage.LOGOUT_SUCCESSFUL,
                    null
            ));

        } catch (UserNotFoundException e) {
            throw new CustomException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/me/state")
    @Operation(
            summary = "회원 탈퇴 (Soft Delete)",
            description = "특정 문구를 입력한 사용자가 요청하면 회원 상태를 DELETED로 바꾸고 탈퇴 처리합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 처리가 완료되었습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "403", description = "유효하지 않은 계정입니다."),
            @ApiResponse(responseCode = "404", description = "관련된 회원이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 에러가 발생했습니다.")
    })
    public ResponseEntity<WrappedDTO<UserWithdrawInfoWrapper>> withdrawUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            UserWithdrawInfoWrapper response = userService.withdrawUser(userDetails.getUser().getId());
            return ResponseEntity.ok(new WrappedDTO<>(
                    true,
                    UserResponseMessage.WITHDRAWAL_SUCCESSFUL,
                    response
            ));

        } catch (InactiveUserException e) {
            throw new CustomException(HttpStatus.FORBIDDEN, ResponseMessage.USER_FORBIDDEN);
        } catch (UserNotFoundException e) {
            throw new CustomException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
