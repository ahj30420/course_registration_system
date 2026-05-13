package com.project.course_registration_system.core.course.dto;

import com.project.course_registration_system.core.course.domain.CourseStatus;
import lombok.Builder;

@Builder
public record CourseSummaryResponse(
        Long id,
        String title,
        Long price,
        CourseStatus status
) { }
