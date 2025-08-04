package com.example.dotdot.global.exception.search;

import com.example.dotdot.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GoogleSearchErrorCode implements ErrorCode {
    API_RESPONSE_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "Google 검색 응답이 null입니다.", "GOOGLE-001"),
    API_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "Google 검색 응답 형식이 올바르지 않습니다.", "GOOGLE-002"),
    API_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Google 검색 API 호출에 실패했습니다.", "GOOGLE-003");

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
