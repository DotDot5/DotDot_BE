package com.example.dotdot.global.exception.chatbot;

import com.example.dotdot.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatbotErrorCode implements ErrorCode {

    GPT_RESPONSE_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, "GPT 응답이 비어 있습니다.", "GPT-001"),
    GPT_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GPT 호출 중 오류가 발생했습니다.", "GPT-002"),
    REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 작업 중 오류가 발생했습니다.", "REDIS-001"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
