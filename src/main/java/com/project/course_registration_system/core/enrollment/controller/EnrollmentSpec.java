package com.project.course_registration_system.core.enrollment.controller;

import com.project.course_registration_system.common.response.ApiResponse;
import com.project.course_registration_system.core.enrollment.dto.EnrollmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "수강 등록 API", description = "수강 등록 관련 API 명세")
public interface EnrollmentSpec {

    @Operation(
            summary = "수강 신청",
            description = "수강 신청을 생성합니다. 생성자는 헤더(USER-ID)로 전달됩니다."
    )
    ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @Parameter(
                    description = "강의 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long courseId,

            @Parameter(
                    description = "수강생 ID (Header)",
                    required = true,
                    example = "1"
            )
            @RequestHeader("USER-ID") Long userId
    );


    @Operation(
            summary = "수강 신청",
            description = "수강 신청을 생성합니다. 생성자는 헤더(USER-ID)로 전달됩니다."
    )
    ResponseEntity<ApiResponse<EnrollmentResponse>> confirm(
            @Parameter(
                    description = "수강 신청 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long enrollmentId,

            @Parameter(
                    description = "수강생 ID (Header)",
                    required = true,
                    example = "1"
            )
            @RequestHeader("USER-ID") Long userId
    );
}
