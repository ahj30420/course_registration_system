package com.project.course_registration_system.core.fixtures;

import com.project.course_registration_system.core.enrollment.domain.Waitlist;

public class WaitlistTestFixture {
    public static Waitlist create() {
        return Waitlist.builder()
                .userId(2L)
                .courseId(1L)
                .build();
    }
}
