package com.project.course_registration_system.core.enrollment.dto;

import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import java.time.LocalDateTime;

public record CourseEnrollmentResponse(
        Long courseId,
        Long enrollmentId,
        Long userId,
        EnrollmentStatus status,
        LocalDateTime createdAt
) {
    public static CourseEnrollmentResponse from(Enrollment enrollment) {
        return new CourseEnrollmentResponse(
                enrollment.getCourse().getId(),
                enrollment.getId(),
                enrollment.getUserId(),
                enrollment.getStatus(),
                enrollment.getCreatedAt()
        );
    }
}
