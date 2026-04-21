package com.project.course_registration_system.core.enrollment.dto;

import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import java.time.LocalDateTime;

public record MyEnrollmentResponse(
        Long enrollmentId,
        Long courseId,
        Long userId,
        EnrollmentStatus status,
        LocalDateTime createdAt,
        CourseBriefResponse course
) {
    public static MyEnrollmentResponse from(Enrollment enrollment) {
        return new MyEnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getUserId(),
                enrollment.getStatus(),
                enrollment.getCreatedAt(),
                CourseBriefResponse.from(enrollment.getCourse())
        );
    }
}
