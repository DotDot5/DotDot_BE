package com.example.dotdot.global.exception.team;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class TeamNotFoundException extends AppException {
    public TeamNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
