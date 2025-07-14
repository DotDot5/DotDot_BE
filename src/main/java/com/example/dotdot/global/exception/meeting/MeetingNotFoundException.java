package com.example.dotdot.global.exception.meeting;

import com.example.dotdot.global.exception.AppException;
import com.example.dotdot.global.exception.ErrorCode;

public class MeetingNotFoundException extends AppException {
    public MeetingNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
