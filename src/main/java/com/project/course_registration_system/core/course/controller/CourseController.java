package com.project.course_registration_system.core.course.controller;

import com.project.course_registration_system.common.response.ApiResponse;
import com.project.course_registration_system.common.util.UrlCreator;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.UpdateCourseStatusRequest;
import com.project.course_registration_system.core.course.dto.CreateCourseRequest;
import com.project.course_registration_system.core.course.dto.CourseResponse;
import com.project.course_registration_system.core.course.dto.CourseSummaryResponse;
import com.project.course_registration_system.core.course.service.CourseService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseController implements CourseSpec {

    private static final String DEFAULT = "/api/courses";
    private final CourseService courseService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> create(
            @Valid @RequestBody CreateCourseRequest request,
            @RequestHeader("CREATOR-ID") Long creatorId
    ) {
        CourseResponse data = courseService.create(request, creatorId);
        URI location = UrlCreator.createUri(DEFAULT, data.id());
        ApiResponse<CourseResponse> response = new ApiResponse<>(data);
        return ResponseEntity.created(location).body(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> getList(
            @RequestParam(required = false) CourseStatus status
    ) {
        List<CourseSummaryResponse> data = courseService.getList(status);
        ApiResponse<List<CourseSummaryResponse>> response = new ApiResponse<>(data);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getDetail(
            @PathVariable Long courseId
    ) {
        CourseResponse data = courseService.getDetail(courseId);
        ApiResponse<CourseResponse> response = new ApiResponse<>(data);
        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{courseId}/status")
    public ResponseEntity<ApiResponse<CourseResponse>> changeStatus(
            @PathVariable Long courseId,
            @RequestHeader("CREATOR-ID") Long creatorId,
            @Valid @RequestBody UpdateCourseStatusRequest request
    ) {
        CourseResponse data = courseService.changeStatus(courseId, creatorId, request.status());
        ApiResponse<CourseResponse> response = new ApiResponse<>(data);
        return ResponseEntity.ok(response);
    }
}
