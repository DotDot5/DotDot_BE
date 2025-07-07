package com.example.dotdot.global.exception.user;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class ImageUploadFailException extends AppException {
    public ImageUploadFailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
