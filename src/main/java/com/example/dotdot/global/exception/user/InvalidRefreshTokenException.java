package com.example.dotdot.global.exception.user;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class InvalidRefreshTokenException extends AppException {
    public InvalidRefreshTokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
