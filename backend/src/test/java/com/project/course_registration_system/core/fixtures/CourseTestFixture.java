package com.project.course_registration_system.core.fixtures;

import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import java.time.LocalDate;

public class CourseTestFixture {

    public static Course create(CourseStatus status) {
        return Course.builder()
                .title("테스트")
                .description("테스트입니다.")
                .price(1000L)
                .capacity(10)
                .creatorId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(status)
                .build();
    }

    public static Course openCourse(int capacity) {
        return Course.builder()
                .title("테스트")
                .description("테스트입니다.")
                .price(1000L)
                .capacity(capacity)
                .creatorId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(CourseStatus.OPEN)
                .build();
    }

}
