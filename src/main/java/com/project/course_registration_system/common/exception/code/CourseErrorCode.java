package com.project.course_registration_system.common.exception.code;

import com.project.course_registration_system.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CourseErrorCode implements ErrorCode {

    INVALID_COURSE_PERIOD(HttpStatus.BAD_REQUEST, "수강 시작일은 종료일보다 늦을 수 없습니다."),
    INVALID_COURSE_CAPACITY(HttpStatus.BAD_REQUEST, "정원은 1명 이상이어야 합니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return null;
    }

    @Override
    public String getMessage() {
        return "";
    }
}
