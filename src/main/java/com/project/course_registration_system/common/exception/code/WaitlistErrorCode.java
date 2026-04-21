package com.project.course_registration_system.common.exception.code;

import com.project.course_registration_system.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum WaitlistErrorCode implements ErrorCode {

    WAITLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 신청 내역이 존재하지 않습니다."),
    NOT_WAITLIST_OWNER(HttpStatus.FORBIDDEN, "해당 대기열에 대한 권한이 없습니다.");

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