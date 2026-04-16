package com.project.course_registration_system.core.course.controller;

import com.project.course_registration_system.common.response.ApiResponse;
import com.project.course_registration_system.common.util.UrlCreator;
import com.project.course_registration_system.core.course.dto.CourseResponse;
import com.project.course_registration_system.core.course.dto.CreateCourseRequest;
import com.project.course_registration_system.core.course.service.CourseService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
