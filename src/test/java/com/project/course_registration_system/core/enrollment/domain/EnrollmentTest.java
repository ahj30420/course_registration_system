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

    @Test
    @DisplayName("활성 상태 확인 테스트: PENDING 또는 CONFIRMED일 때 true")
    void is_active_true_when_pending_or_confirmed() throws Exception {
        // given
        Enrollment pending = Enrollment.builder().status(EnrollmentStatus.PENDING).build();
        Enrollment confirmed = Enrollment.builder().status(EnrollmentStatus.CONFIRMED).build();
        Enrollment cancelled = Enrollment.builder().status(EnrollmentStatus.CANCELLED).build();

        // when & then
        assertThat(pending.isActive()).isTrue();
        assertThat(confirmed.isActive()).isTrue();
        assertThat(cancelled.isActive()).isFalse();
    }

    @Test
    @DisplayName("수강 취소 테스트: 성공[결제 대기 상태일 경우]")
    void cancel_success_when_pending() throws Exception {
        // given
        Enrollment enrollment = Enrollment.builder()
                .status(EnrollmentStatus.PENDING)
                .build();

        // when
        enrollment.cancel(7);

        // then
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(enrollment.getCancelAt()).isNotNull();
    }

    @Test
    @DisplayName("수강 취소 테스트: 성공[결제 완료 후 취소 가능 기간일 경우]")
    void cancel_success_within_period() throws Exception {
        // given
        Enrollment enrollment = Enrollment.builder()
                .status(EnrollmentStatus.PENDING)
                .build();
        enrollment.confirm();

        // when
        enrollment.cancel(7);

        // then
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("수강 취소 테스트: 실패[이미 취소된 경우]")
    void cancel_fail_already_cnacelled() throws Exception {
        // given
        Enrollment enrollment = Enrollment.builder()
                .status(EnrollmentStatus.CANCELLED)
                .build();

        // when & then
        assertThatThrownBy(() -> enrollment.cancel(7))
                .isInstanceOf(BaseException.class)
                .hasMessage(EnrollmentErrorCode.ENROLLMENT_NOT_CANCELLABLE.getMessage());
    }

    @Test
    @DisplayName("수강 취소 테스트: 실패[취소 가능 기간이 경과한 경우]")
    void cance_fail_expired_period() throws Exception {
        // given
        Enrollment enrollment = Enrollment.builder()
                .status(EnrollmentStatus.CONFIRMED)
                .build();

        // when & then
        assertThatThrownBy(() -> enrollment.cancel(7))
                .isInstanceOf(BaseException.class)
                .hasMessage(EnrollmentErrorCode.CANCELLATION_PERIOD_EXPIRED.getMessage());
    }
}