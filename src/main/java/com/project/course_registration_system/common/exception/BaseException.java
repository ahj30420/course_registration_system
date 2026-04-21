package com.project.course_registration_system.common.exception;

import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() { return errorCode.getHttpStatus(); }

    public String getMessage() { return errorCode.getMessage(); }


}
