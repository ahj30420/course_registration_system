package com.project.course_registration_system.core.course.controller;

import com.project.course_registration_system.common.response.ApiResponse;
import com.project.course_registration_system.common.util.UrlCreator;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.response.CourseResponse;
import com.project.course_registration_system.core.course.dto.request.CreateCourseRequest;
import com.project.course_registration_system.core.course.dto.response.CourseSummaryResponse;
import com.project.course_registration_system.core.course.service.CourseService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
        ApiResponse<CourseResponse> response = ApiResponse.<CourseResponse>builder()
                .body(data)
                .build();
        return ResponseEntity.created(location).body(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> getList(
            @RequestParam(required = false) CourseStatus status
    ) {
        List<CourseSummaryResponse> data = courseService.getList(status);
        ApiResponse<List<CourseSummaryResponse>> response = ApiResponse.<List<CourseSummaryResponse>>builder()
                .body(data)
                .build();
        return ResponseEntity.ok(response);
    }

}
