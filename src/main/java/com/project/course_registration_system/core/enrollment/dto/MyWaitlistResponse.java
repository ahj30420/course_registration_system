package com.project.course_registration_system.core.enrollment.dto;

import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import com.project.course_registration_system.core.enrollment.domain.Waitlist;
import java.time.LocalDateTime;

public record MyWaitlistResponse(
        Long waitlistId,
        Long courseId,
        Long userId,
        EnrollmentStatus status,
        LocalDateTime createdAt,
        CourseBriefResponse course
) {
    public static MyWaitlistResponse from(Waitlist waitlist) {
        return new MyWaitlistResponse(
                waitlist.getId(),
                waitlist.getCourse().getId(),
                waitlist.getUserId(),
                EnrollmentStatus.WAITLISTED,
                waitlist.getCreatedAt(),
                CourseBriefResponse.from(waitlist.getCourse())
        );
    }
}
