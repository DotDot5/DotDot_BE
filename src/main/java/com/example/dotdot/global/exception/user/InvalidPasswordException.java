package com.example.dotdot.global.exception.user;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class InvalidPasswordException extends AppException {
    public InvalidPasswordException(ErrorCode errorCode) {
        super(errorCode);
    }
}
