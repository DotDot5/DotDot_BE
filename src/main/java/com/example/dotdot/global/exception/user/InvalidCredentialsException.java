package com.example.dotdot.global.exception.user;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
