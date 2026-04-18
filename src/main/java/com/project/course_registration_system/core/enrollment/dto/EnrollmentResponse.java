package com.project.course_registration_system.core.enrollment.dto;

import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import com.project.course_registration_system.core.enrollment.domain.Waitlist;
import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long enrollmentId,
        Long courseId,
        Long userId,
        EnrollmentStatus status,
        LocalDateTime createdAt
) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getUserId(),
                enrollment.getStatus(),
                enrollment.getCreatedAt()
        );
    }

    public static EnrollmentResponse from(Waitlist waitlist) {
        return new EnrollmentResponse(
                waitlist.getId(),
                waitlist.getCourseId(),
                waitlist.getUserId(),
                EnrollmentStatus.WAITLISTED,
                waitlist.getCreatedAt()
        );
    }
}
