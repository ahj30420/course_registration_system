package com.project.course_registration_system.core.enrollment.controller;

import com.project.course_registration_system.common.response.ApiResponse;
import com.project.course_registration_system.common.util.UrlCreator;
import com.project.course_registration_system.core.enrollment.dto.EnrollmentResponse;
import com.project.course_registration_system.core.enrollment.service.EnrollmentService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EnrollmentController implements EnrollmentSpec {

    private static final String DEFAULT = "/api/enrollment";
    private final EnrollmentService enrollmentService;

    @Override
    @PostMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @PathVariable Long courseId,
            @RequestHeader("USER-ID") Long userId
    ) {
        EnrollmentResponse data = enrollmentService.enroll(courseId, userId);
        URI location = UrlCreator.createUri(DEFAULT, data.enrollmentId());
        ApiResponse<EnrollmentResponse> apiResponse = new ApiResponse<>(data);
        return ResponseEntity.created(location).body(apiResponse);
    }

    @Override
    @PostMapping("/{enrollmentId}/confirm")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> confirm(
            @PathVariable Long enrollmentId,
            @RequestHeader("USER-ID") Long userId
    ) {
        EnrollmentResponse data = enrollmentService.confirm(enrollmentId, userId);
        ApiResponse<EnrollmentResponse> apiResponse = new ApiResponse<>(data);
        return ResponseEntity.ok().body(apiResponse);
    }

    @Override
    @PostMapping("/{enrollmentId}/cancel")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> cancel(
            @PathVariable Long enrollmentId,
            @RequestHeader("USER-ID") Long userId
    ) {
        EnrollmentResponse data = enrollmentService.cancel(enrollmentId, userId);
        ApiResponse<EnrollmentResponse> apiResponse = new ApiResponse<>(data);
        return ResponseEntity.ok().body(apiResponse);
    }
}
