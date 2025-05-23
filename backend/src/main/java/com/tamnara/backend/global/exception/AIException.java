package com.tamnara.backend.global.exception;

import com.tamnara.backend.global.dto.WrappedDTO;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class AIException extends RuntimeException {
    private final WrappedDTO<?> errorBody;
    private final HttpStatusCode status;

    public AIException(HttpStatusCode status, WrappedDTO<?> errorBody) {
        super("AI 처리 실패: " + errorBody.getMessage());
        this.status = status;
        this.errorBody = errorBody;
    }

}
