package com.project.course_registration_system.core.enrollment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.Waitlist;
import com.project.course_registration_system.core.enrollment.dto.EnrollmentResponse;
import com.project.course_registration_system.core.enrollment.repository.EnrollmentRepository;
import com.project.course_registration_system.core.enrollment.repository.WaitlistRepository;
import com.project.course_registration_system.core.fixtures.CourseTestFixture;
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

        for(int i=0; i<5; i++) course.incrementEnrollmentCount();

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

}