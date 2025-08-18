package com.example.dotdot.global.exception.bookmark;

import com.example.dotdot.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BookmarkErrorCode implements ErrorCode {

    SPEECH_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 speechlog가 존재하지 않습니다.", "BOOKMARK-001"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
