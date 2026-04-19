package com.project.course_registration_system.core.fixtures;

import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;

public class EnrollmentTestFixture {

    public static Enrollment create(EnrollmentStatus status) {
        return Enrollment.builder()
                .userId(1L)
                .course(CourseTestFixture.openCourse(10))
                .status(status)
                .build();
    }
}
