package com.project.course_registration_system.core.enrollment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.project.course_registration_system.common.exception.BaseException;
import com.project.course_registration_system.common.exception.code.EnrollmentErrorCode;
import com.project.course_registration_system.core.fixtures.CourseTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnrollmentTest {

    @Test
    @DisplayName("결제 완료 처리 테스트: 성공")
    void change_status_to_confirm_success() throws Exception {
        // given
        Enrollment enrollment = Enrollment.builder()
                .userId(1L)
                .course(CourseTestFixture.openCourse(10))
                .status(EnrollmentStatus.PENDING)
                .build();

        // when
        enrollment.confirm();

        // then
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("결제 완료 처리 테스트: 실패[결제 대기 상태가 아닌 경우]")
    void change_status_fail_when_status_not_pending() throws Exception {
        // given
        Enrollment enrollment = Enrollment.builder()
                .userId(1L)
                .course(CourseTestFixture.openCourse(10))
                .status(EnrollmentStatus.CONFIRMED)
                .build();

        // when & then
        assertThatThrownBy(enrollment::confirm)
                .isInstanceOf(BaseException.class)
                .hasMessage(EnrollmentErrorCode.ENROLLMENT_NOT_PENDING.getMessage());
    }


    @Test
    @DisplayName("수강 내역 권한 테스트: 성공")
    void owner_check_success() throws Exception {
        // given
        Long userId = 1L;
        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .course(CourseTestFixture.openCourse(10))
                .status(EnrollmentStatus.PENDING)
                .build();

        // when
        boolean isOnwer = enrollment.isOwner(userId);

        // then
        assertThat(isOnwer).isTrue();
    }

}