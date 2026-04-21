package com.project.course_registration_system.core.enrollment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.course_registration_system.core.fixtures.CourseTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitlistTest {

    @Test
    @DisplayName("대기열 권한 테스트: 성공")
    void owner_check_success() throws Exception {
        // given
        Long userId = 1L;
        Waitlist waitlist = Waitlist.builder()
                .course(CourseTestFixture.openCourse(10))
                .userId(userId)
                .build();

        // when
        boolean isOnwer = waitlist.isOwner(userId);

        // then
        assertThat(isOnwer).isTrue();
    }

}