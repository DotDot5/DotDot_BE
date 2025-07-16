package com.example.dotdot.global.exception.meeting;

import com.example.dotdot.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MeetingErrorCode implements ErrorCode {
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회의를 찾을 수 없습니다.", "MEETING-001");

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}