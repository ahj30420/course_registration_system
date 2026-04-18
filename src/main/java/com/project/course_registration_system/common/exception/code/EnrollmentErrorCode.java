package com.project.course_registration_system.common.exception.code;

import com.project.course_registration_system.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum EnrollmentErrorCode implements ErrorCode {

    COURSE_NOT_OPEN(HttpStatus.BAD_REQUEST, "OPEN 상태 강의만 신청 가능합니다."),
    COURSE_FULL(HttpStatus.BAD_REQUEST, "정원이 가득 찼습니다."),
    ALREADY_ENROLLED(HttpStatus.BAD_REQUEST, "이미 신청한 강의입니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}