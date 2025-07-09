package com.example.dotdot.global.exception.team;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class AlreadyJoinedTeamException extends AppException {
    public AlreadyJoinedTeamException(ErrorCode errorCode) {
        super(errorCode);
    }
}
