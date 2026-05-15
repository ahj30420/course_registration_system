package com.project.course_registration_system.common.exception.code;

import com.project.course_registration_system.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() { return status; }

    @Override
    public String getMessage() { return message; }

}