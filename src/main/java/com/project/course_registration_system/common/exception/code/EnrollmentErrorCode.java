package com.project.course_registration_system.common.exception.code;

import com.project.course_registration_system.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum EnrollmentErrorCode implements ErrorCode {

    COURSE_NOT_OPEN(HttpStatus.BAD_REQUEST, "OPEN 상태 강의만 신청 가능합니다."),
    COURSE_FULL(HttpStatus.BAD_REQUEST, "정원이 가득 찼습니다."),
    ALREADY_ENROLLED(HttpStatus.BAD_REQUEST, "이미 신청한 강의입니다."),
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 신청 내역이 존재하지 않습니다."),
    ENROLLMENT_NOT_PENDING(HttpStatus.BAD_REQUEST, "결제 대기 상태가 아닙니다."),
    NOT_ENROLLMENT_OWNER(HttpStatus.FORBIDDEN, "해당 신청 내역에 대한 권한이 없습니다.");

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