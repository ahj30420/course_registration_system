package com.project.course_registration_system.core.course.dto.response;

import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import java.time.LocalDate;

public record CourseResponse(
        Long id,
        String title,
        String description,
        Long price,
        Integer capacity,
        Long creatorId,
        long currentEnrollmentCount,
        LocalDate startDate,
        LocalDate endDate,
        CourseStatus status
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getCapacity(),
                course.getCreatorId(),
                course.getCurrentEnrollmentCount(),
                course.getStartDate(),
                course.getEndDate(),
                course.getStatus()
        );
    }
}
