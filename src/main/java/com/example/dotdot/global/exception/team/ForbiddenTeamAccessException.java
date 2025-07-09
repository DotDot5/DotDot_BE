package com.example.dotdot.global.exception.team;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class ForbiddenTeamAccessException extends AppException {
    public ForbiddenTeamAccessException(ErrorCode errorCode) {
        super(errorCode);
    }
}
