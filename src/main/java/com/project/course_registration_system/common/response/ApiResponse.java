package com.project.course_registration_system.common.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
public class ApiResponse<T> {
    private final Status status = Status.SUCCESS;
    private T body;
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder
    public ApiResponse(T body) {
        this.body = body;
    }
}
