package com.example.dotdot.global.exception.team;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class UserNotInTeamException extends AppException {
    public UserNotInTeamException(ErrorCode errorCode) {
        super(errorCode);
    }
}
