package com.project.course_registration_system.core.course.service.fixtures;

import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import java.time.LocalDate;

public class CourseTestFixture {

    public static Course create() {
        return Course.builder()
                .title("테스트")
                .description("테스트입니다.")
                .price(1000L)
                .capacity(10)
                .creatorId(1L)
                .startDate(LocalDate.of(2026, 4, 16))
                .endDate(LocalDate.of(2026, 4, 21))
                .status(CourseStatus.DRAFT)
                .build();
    }

}
