package com.example.dotdot.global.exception.team;

import com.example.dotdot.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TeamErrorCode implements ErrorCode {
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다.", "TEAM-001"),
    ALREADY_JOINED_TEAM(HttpStatus.BAD_REQUEST, "이미 해당 팀에 가입되어 있습니다.", "TEAM-002"),
    USER_NOT_IN_TEAM(HttpStatus.NOT_FOUND, "해당 팀에 소속된 사용자를 찾을 수 없습니다.", "TEAM-003"),
    FORBIDDEN_TEAM_ACCESS(HttpStatus.FORBIDDEN, "해당 팀에 접근할 권한이 없습니다.", "TEAM-004");

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
