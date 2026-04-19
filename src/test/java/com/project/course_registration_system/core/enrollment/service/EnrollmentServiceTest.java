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
import com.project.course_registration_system.common.response.PageResponse;
import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import com.project.course_registration_system.core.enrollment.domain.Waitlist;
import com.project.course_registration_system.core.enrollment.dto.EnrollmentResponse;
import com.project.course_registration_system.core.enrollment.dto.MyEnrollmentResponse;
import com.project.course_registration_system.core.enrollment.dto.MyWaitlistResponse;
import com.project.course_registration_system.core.enrollment.repository.EnrollmentRepository;
import com.project.course_registration_system.core.enrollment.repository.WaitlistRepository;
import com.project.course_registration_system.core.fixtures.CourseTestFixture;
import com.project.course_registration_system.core.fixtures.EnrollmentTestFixture;
import com.project.course_registration_system.core.fixtures.WaitlistTestFixture;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Test
    @DisplayName("수강 취소 테스트: 성공[PENDING 상태 취소 시 정원 감소 및 대기열 승격 확인]")
    void cancel_success_and_promote() throws Exception {
        // given
        Long enrollmentId = 1L;
        Long userId = 1L;

        Course course = CourseTestFixture.openCourse(10);
        course.incrementEnrollmentCount();

        Enrollment enrollment = EnrollmentTestFixture.create(EnrollmentStatus.PENDING);
        Waitlist nextUser = WaitlistTestFixture.create();

        given(enrollmentRepository.findById(enrollmentId)).willReturn(Optional.of(enrollment));
        given(courseRepository.findByIdForUpdate(course.getId())).willReturn(Optional.of(course));
        given(waitlistRepository.findFirstByCourseIdOrderByCreatedAtAsc(course.getId()))
                .willReturn(Optional.of(nextUser));

        // when
        EnrollmentResponse response = sut.cancel(enrollmentId, userId);

        // then
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(course.getCurrentEnrollmentCount()).isEqualTo(1);

        verify(waitlistRepository).delete(nextUser);
        verify(enrollmentRepository).save(any(Enrollment.class));
        assertThat(response.status()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("수강 취소 테스트: 성공[대기자가 없는 경우 정원만 감소]")
    void cancel_success_no_waitlist() throws Exception {
        // given
        Long enrollmentId = 1L;
        Long userId = 1L;

        Course course = CourseTestFixture.openCourse(10);
        course.incrementEnrollmentCount();

        Enrollment enrollment = EnrollmentTestFixture.create(EnrollmentStatus.PENDING);

        given(enrollmentRepository.findById(enrollmentId)).willReturn(Optional.of(enrollment));
        given(courseRepository.findByIdForUpdate(course.getId())).willReturn(Optional.of(course));
        given(waitlistRepository.findFirstByCourseIdOrderByCreatedAtAsc(course.getId()))
                .willReturn(Optional.empty());

        // when
        EnrollmentResponse response = sut.cancel(enrollmentId, userId);

        // then
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(course.getCurrentEnrollmentCount()).isEqualTo(0);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
        assertThat(response.status()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("수강 취소 테스트: 실패[타인의 신청 내역인 경우]")
    void cancel_fail_when_is_not_owner() throws Exception {
        // given
        Long enrollmentId = 1L;
        Long otherUserId = 99L;
        Enrollment enrollment = EnrollmentTestFixture.create(EnrollmentStatus.PENDING);

        given(enrollmentRepository.findById(enrollmentId)).willReturn(Optional.of(enrollment));

        // when & then
        assertThatThrownBy(() -> sut.cancel(enrollmentId, otherUserId))
                .isInstanceOf(BaseException.class)
                .hasMessage(EnrollmentErrorCode.NOT_ENROLLMENT_OWNER.getMessage());
    }

    @Test
    @DisplayName("내 수강 신청 목록 테스트: 성공")
    void getMyEnrollments_returns_page() {
        // tdd
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Enrollment enrollment = EnrollmentTestFixture.create(EnrollmentStatus.PENDING);
        given(enrollmentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                .willReturn(new PageImpl<>(List.of(enrollment), pageable, 1));

        // when
        PageResponse<MyEnrollmentResponse> result = sut.getMyEnrollments(userId, pageable);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.content().getFirst().enrollmentId()).isEqualTo(enrollment.getId());
    }

    @Test
    @DisplayName("내 대기열 목록 테스트: 성공")
    void getMyWaitlist_returns_page() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Waitlist waitlist = WaitlistTestFixture.create();
        given(waitlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                .willReturn(new PageImpl<>(List.of(waitlist), pageable, 1));

        // when
        PageResponse<MyWaitlistResponse> result = sut.getMyWaitlist(userId, pageable);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.content().getFirst().waitlistId()).isEqualTo(waitlist.getId());
    }

}