package com.tamnara.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserUpdateRequestDto {

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    private String nickname;
}
