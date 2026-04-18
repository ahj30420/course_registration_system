package com.project.course_registration_system.core.enrollment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.course_registration_system.common.exception.BaseException;
import com.project.course_registration_system.common.exception.code.EnrollmentErrorCode;
import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import com.project.course_registration_system.core.enrollment.domain.Waitlist;
import com.project.course_registration_system.core.enrollment.dto.EnrollmentResponse;
import com.project.course_registration_system.core.enrollment.repository.EnrollmentRepository;
import com.project.course_registration_system.core.enrollment.repository.WaitlistRepository;
import com.project.course_registration_system.core.fixtures.CourseTestFixture;
import com.project.course_registration_system.core.fixtures.EnrollmentTestFixture;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @InjectMocks
    private EnrollmentService sut;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private WaitlistRepository waitlistRepository;


    @Test
    @DisplayName("수강 신청 테스트: 성공[정원이 남았으면 PENDING 상태로 저장]")
    void enroll_success_when_has_capacity() throws Exception {
        // given
        Long courseId = 1L;
        Long userId = 1L;

        Course course = CourseTestFixture.openCourse(10);

        given(courseRepository.findByIdForUpdate(courseId))
                .willReturn(Optional.of(course));

        given(enrollmentRepository.existsByCourseIdAndUserIdAndStatusIn(any(), any(), any()))
                .willReturn(false);

        // when
        EnrollmentResponse response = sut.enroll(courseId, userId);

        // then
        assertThat(course.getCurrentEnrollmentCount()).isEqualTo(1);
        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(waitlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("수강 신청 테스트: 성공[정원이 꽉 찼으면 대기열에 저장]")
    void enroll_to_waitlist_when_full() throws Exception {
        // given
        Long courseId = 1L;
        Long userId = 1L;

        Course course = CourseTestFixture.openCourse(5);

        for (int i = 0; i < 5; i++) {
            course.incrementEnrollmentCount();
        }

        given(courseRepository.findByIdForUpdate(courseId))
                .willReturn(Optional.of(course));

        given(enrollmentRepository.existsByCourseIdAndUserIdAndStatusIn(any(), any(), any()))
                .willReturn(false);

        // when
        EnrollmentResponse response = sut.enroll(courseId, userId);

        // then
        assertThat(course.getCurrentEnrollmentCount()).isEqualTo(5);
        verify(waitlistRepository).save(any(Waitlist.class));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("결제 상태 변경 테스트: 성공")
    void confirm_success() throws Exception {
        // given
        Long enrollmentId = 1L;
        Long userId = 1L;
        EnrollmentStatus newStatus = EnrollmentStatus.CONFIRMED;

        Enrollment enrollment = EnrollmentTestFixture.create(EnrollmentStatus.PENDING);

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        // when
        EnrollmentResponse response = sut.confirm(enrollmentId, userId);

        // then
        assertThat(response.status()).isEqualTo(newStatus);
        assertThat(enrollment.getStatus()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("결제 상태 변경 테스트: 실패[해당 수강 신청에 권한이 없는 경우]")
    void confirm_fail_when_is_not_owner() throws Exception {
        // given
        Long enrollmentId = 1L;
        Long userId = 2L;
        EnrollmentStatus newStatus = EnrollmentStatus.CONFIRMED;

        Enrollment enrollment = EnrollmentTestFixture.create(EnrollmentStatus.PENDING);

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        // when & then
        assertThatThrownBy(() ->
                sut.confirm(enrollmentId, userId))
                .isInstanceOf(BaseException.class)
                .hasMessage(EnrollmentErrorCode.NOT_ENROLLMENT_OWNER.getMessage());
    }

    @Test
    @DisplayName("결제 상태 변경 테스트: 실패[결제 대기 상태가 아닌 경우]")
    void confirm_fail_not_found() throws Exception {
        // given
        Long enrollmentId = 9999L;
        Long userId = 1L;

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                sut.confirm(enrollmentId, userId))
                .isInstanceOf(BaseException.class)
                .hasMessage(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND.getMessage());
    }

}