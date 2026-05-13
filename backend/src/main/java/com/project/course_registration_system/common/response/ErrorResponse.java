package com.project.course_registration_system.common.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorResponse {
    private final Status status = Status.FAIL;
    private final HttpStatus httpStatus;
    private final String message;

    @Builder
    public ErrorResponse(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
