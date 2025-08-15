package com.example.dotdot.global.exception.task;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class TaskNotFoundException extends AppException {
    public TaskNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
