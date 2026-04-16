package com.project.course_registration_system.core.course.controller;

import com.project.course_registration_system.common.response.ApiResponse;
import com.project.course_registration_system.core.course.dto.CourseResponse;
import com.project.course_registration_system.core.course.dto.CreateCourseRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "강의 API", description = "강의 관련 API 명세")
public interface CourseSpec {

    @Operation(
            summary = "강의 생성",
            description = "강의를 생성합니다. 생성자는 헤더(CREATOR-ID)로 전달됩니다."
    )
    ResponseEntity<ApiResponse<CourseResponse>> create(
            @Parameter(
                    description = "강의 생성 요청 정보",
                    required = true
            )
            @RequestBody CreateCourseRequest request,

            @Parameter(
                    description = "강사 ID (Header)",
                    required = true,
                    example = "1"
            )
            @RequestHeader("CREATOR-ID") Long creatorId
    );
}
