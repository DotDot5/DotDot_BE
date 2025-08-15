package com.example.dotdot.global.exception.task;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class AssigneeNotInTeamException extends AppException {
    public AssigneeNotInTeamException(ErrorCode errorCode) {
        super(errorCode);
    }
}
