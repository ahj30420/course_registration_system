package com.project.course_registration_system.core.enrollment.controller;

import com.project.course_registration_system.common.response.ApiResponse;
import com.project.course_registration_system.common.response.PageResponse;
import com.project.course_registration_system.common.util.UrlCreator;
import com.project.course_registration_system.core.enrollment.dto.EnrollmentResponse;
import com.project.course_registration_system.core.enrollment.dto.MyEnrollmentResponse;
import com.project.course_registration_system.core.enrollment.dto.MyWaitlistResponse;
import com.project.course_registration_system.core.enrollment.service.EnrollmentService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
        ApiResponse<EnrollmentResponse> response = new ApiResponse<>(data);
        return ResponseEntity.created(location).body(response);
    }

    @Override
    @PostMapping("/{enrollmentId}/confirm")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> confirm(
            @PathVariable Long enrollmentId,
            @RequestHeader("USER-ID") Long userId
    ) {
        EnrollmentResponse data = enrollmentService.confirm(enrollmentId, userId);
        ApiResponse<EnrollmentResponse> response = new ApiResponse<>(data);
        return ResponseEntity.ok().body(response);
    }

    @Override
    @PostMapping("/{enrollmentId}/cancel")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> cancel(
            @PathVariable Long enrollmentId,
            @RequestHeader("USER-ID") Long userId
    ) {
        EnrollmentResponse data = enrollmentService.cancel(enrollmentId, userId);
        ApiResponse<EnrollmentResponse> response = new ApiResponse<>(data);
        return ResponseEntity.ok().body(response);
    }

    @Override
    @GetMapping("/enrollments/my")
    public ResponseEntity<ApiResponse<PageResponse<MyEnrollmentResponse>>> getMyEnrollments(
            @RequestHeader("USER-ID") Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<MyEnrollmentResponse> data = enrollmentService.getMyEnrollments(userId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(data));
    }

    @Override
    @GetMapping("/enrollments/my/waitlist")
    public ResponseEntity<ApiResponse<PageResponse<MyWaitlistResponse>>> getMyWaitlist(
            @RequestHeader("USER-ID") Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<MyWaitlistResponse> data = enrollmentService.getMyWaitlist(userId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(data));
    }
}
