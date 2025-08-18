package com.example.dotdot.global.exception.task;

import com.example.dotdot.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TaskErrorCode implements ErrorCode {
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 작업입니다.", "TASK-001"),
    FORBIDDEN_TASK_ACCESS(HttpStatus.FORBIDDEN, "해당 작업에 접근할 권한이 없습니다.", "TASK-002"),
    INVALID_TASK_STATUS(HttpStatus.BAD_REQUEST, "잘못된 작업 상태입니다.", "TASK-003"),
    INVALID_TASK_PRIORITY(HttpStatus.BAD_REQUEST, "잘못된 작업 우선순위입니다.", "TASK-004"),
    USER_NOT_IN_TEAM(HttpStatus.BAD_REQUEST, "담당자가 팀에 소속되어 있지 않습니다.", "TASK-005");

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
