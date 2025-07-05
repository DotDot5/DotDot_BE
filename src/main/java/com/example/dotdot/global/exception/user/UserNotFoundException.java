package com.example.dotdot.global.exception.user;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class UserNotFoundException extends AppException {
    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
