package com.project.course_registration_system.core.course.dto;

import com.project.course_registration_system.core.course.domain.CourseStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCourseStatusRequest(
        @NotNull CourseStatus status
) {
}