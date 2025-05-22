package com.tamnara.backend.global.exception;

import com.tamnara.backend.news.dto.WrappedDTO;
import lombok.Getter;

@Getter
public class AIException extends RuntimeException {
    private final WrappedDTO<?> errorBody;

    public AIException(WrappedDTO<?> errorBody) {
        super("AI 처리 실패: " + errorBody.getMessage());
        this.errorBody = errorBody;
    }

}
