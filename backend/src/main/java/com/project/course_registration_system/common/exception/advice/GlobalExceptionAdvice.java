package com.project.course_registration_system.common.exception.advice;

import com.project.course_registration_system.common.exception.BaseException;
import com.project.course_registration_system.common.exception.code.CommonErrorCode;
import com.project.course_registration_system.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> BaseException(BaseException e) {
        log.warn("[BaseException] {} - {}", e.getHttpStatus().value(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus())
                .body(new ErrorResponse(e.getHttpStatus(), e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> exception(Exception e) {
        HttpStatus errorStatus = CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus();
        log.error("[{}] {}", e.getClass().getName(), e.getMessage());
        return ResponseEntity.status(errorStatus)
                .body(new ErrorResponse(errorStatus, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("[MethodArgumentNotValidException] {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST, errorMessage));
    }

}
