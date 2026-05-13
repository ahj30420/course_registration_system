package com.project.course_registration_system.core.enrollment.dto;

import com.project.course_registration_system.core.course.domain.Course;

public record CourseBriefResponse(
        Long id,
        String title,
        String description
) {
    public static CourseBriefResponse from(Course course) {
        return new CourseBriefResponse(course.getId(), course.getTitle(), course.getDescription());
    }
}
