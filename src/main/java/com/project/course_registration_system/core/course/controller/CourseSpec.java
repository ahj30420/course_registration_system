package com.project.course_registration_system.core.course.controller;

import com.project.course_registration_system.common.response.ApiResponse;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.UpdateCourseStatusRequest;
import com.project.course_registration_system.core.course.dto.response.CourseResponse;
import com.project.course_registration_system.core.course.dto.request.CreateCourseRequest;
import com.project.course_registration_system.core.course.dto.response.CourseSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Operation(
            summary = "강의 목록 조회",
            description = "강의 목록을 조회합니다(with. 강의 상태)"
    )
    ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> getList(
            @Parameter(
                    description = "강의 상태",
                    required = false
            )
            @RequestParam CourseStatus status
    );


    @Operation(
            summary = "강의 상세 조회",
            description = "강의 상세 조회입니다."
    )
    ResponseEntity<ApiResponse<CourseResponse>> getDetail(
            @Parameter(
                    description = "강의 ID",
                    required = true
            )
            @PathVariable Long courseId
    );


    @Operation(
            summary = "강의 상태 변경",
            description = "강의 상태를 변경합니다."
    )
    ResponseEntity<ApiResponse<CourseResponse>> changeStatus(
            @Parameter(
                    description = "강의 ID",
                    required = true
            )
            @PathVariable Long courseId,

            @Parameter(
                    description = "강사 ID (Header)",
                    required = true
            )
            @RequestHeader("CREATOR-ID") Long creatorId,

            @Parameter(
                    description = "변경된 강의 상태",
                    required = true
            )
            @Valid @RequestBody UpdateCourseStatusRequest request
    );
}
