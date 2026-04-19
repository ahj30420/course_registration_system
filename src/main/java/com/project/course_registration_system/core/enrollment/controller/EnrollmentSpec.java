package com.project.course_registration_system.core.enrollment.controller;

import com.project.course_registration_system.common.response.ApiResponse;
import com.project.course_registration_system.common.response.PageResponse;
import com.project.course_registration_system.core.enrollment.dto.EnrollmentResponse;
import com.project.course_registration_system.core.enrollment.dto.MyEnrollmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
            summary = "결제 완료 처리",
            description = "결제 완료 처리합니다. 생성자는 헤더(USER-ID)로 전달됩니다."
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


    @Operation(
            summary = "수강 취소",
            description = "수강 취소합니다. 생성자는 헤더(USER-ID)로 전달됩니다."
    )
    ResponseEntity<ApiResponse<EnrollmentResponse>> cancel(
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

    @Operation(
            summary = "내 수강 신청 목록 조회",
            description = "로그인 사용자(USER-ID)의 수강 신청 내역을 최신순으로 페이지 조회합니다. 강의 제목·설명 등 기본 정보를 포함합니다."
    )
    ResponseEntity<ApiResponse<PageResponse<MyEnrollmentResponse>>> getMyEnrollments(
            @Parameter(
                    description = "수강생 ID (Header)",
                    required = true,
                    example = "1"
            )
            @RequestHeader("USER-ID") Long userId,

            @PageableDefault Pageable pageable
    );
}
