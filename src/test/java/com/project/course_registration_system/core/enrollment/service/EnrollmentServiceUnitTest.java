package com.project.course_registration_system.core.enrollment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import com.project.course_registration_system.core.enrollment.repository.EnrollmentRepository;
import com.project.course_registration_system.core.enrollment.repository.WaitlistRepository;
import com.project.course_registration_system.core.fixtures.CourseTestFixture;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EnrollmentServiceUnitTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @AfterEach
    void tearDown() {
        waitlistRepository.deleteAllInBatch();
        enrollmentRepository.deleteAllInBatch();
        courseRepository.deleteAllInBatch();
    }


    @Test
    @DisplayName("동시 신청 테스트: 정원 5명인 강의에 10명이 신청하면 5명 신청, 5명 대기열")
    void concurrentEnroll() throws Exception {
        // given
        int capacity = 5;
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Course course = courseRepository.save(CourseTestFixture.openCourse(capacity));

        // when
        for (long i = 1; i <= threadCount; i++) {
            final long userId = i;
            executor.submit(() -> {
                try {
                    enrollmentService.enroll(course.getId(), userId);
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
        long enrollmentCount = enrollmentRepository.countByCourseId(course.getId());
        long waitlistCount = waitlistRepository.countByCourseId(course.getId());

        // then
        assertThat(updatedCourse.getCurrentEnrollmentCount()).isEqualTo(capacity);
        assertThat(enrollmentCount).isEqualTo(capacity);
        assertThat(waitlistCount).isEqualTo(threadCount - capacity);
    }

    @Test
    @DisplayName("취소 및 승격 통합 테스트: 성공[수강자가 취소하면 대기열 1번이 자동으로 신청됨]")
    void cancel_and_promote_integration() throws Exception {
        // given
        int capacity = 1;
        Course course = courseRepository.save(CourseTestFixture.openCourse(capacity));

        Long userA = 1L;
        enrollmentService.enroll(course.getId(), userA);

        Long userB = 2L;
        enrollmentService.enroll(course.getId(), userB);

        assertThat(enrollmentRepository.countByCourseId(course.getId())).isEqualTo(1);
        assertThat(waitlistRepository.countByCourseId(course.getId())).isEqualTo(1);

        // when
        enrollmentService.cancel(course.getId(), userA);

        // then
        assertThat(enrollmentRepository.countByCourseIdAndStatusIn(
                course.getId(), List.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED))).isEqualTo(1);
        assertThat(waitlistRepository.countByCourseId(course.getId())).isEqualTo(0);

        boolean isUserBEnrolled = enrollmentRepository.findAll().stream()
                .anyMatch(e -> e.getUserId().equals(userB));
        assertThat(isUserBEnrolled).isTrue();

        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(updatedCourse.getCurrentEnrollmentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("동시 취소 테스트: 여러 명이 동시에 취소해도 대기열 승격이 정확히 인원만큼만 수행됨")
    void concurrent_cancel_and_promote() throws Exception {
        // given
        int capacity = 10;
        int initialEnrollment = 10;
        int waitlistCount = 5;

        Course course = courseRepository.save(CourseTestFixture.openCourse(capacity));

        for (long i = 1; i <= initialEnrollment; i++) {
            enrollmentService.enroll(course.getId(), i);
        }
        for (long i = initialEnrollment + 1; i <= initialEnrollment + waitlistCount; i++) {
            enrollmentService.enroll(course.getId(), i);
        }

        List<Long> enrollmentIdsToCancel = enrollmentRepository.findAll().stream()
                .map(e -> e.getId())
                .limit(3)
                .toList();

        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);

        for (Long enrollmentId : enrollmentIdsToCancel) {
            executor.submit(() -> {
                try {
                    Long ownerId = enrollmentRepository.findById(enrollmentId).get().getUserId();
                    enrollmentService.cancel(enrollmentId, ownerId);
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);

        // then
        assertThat(enrollmentRepository.countByCourseIdAndStatusIn(
                course.getId(), List.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED))).isEqualTo(10);
        assertThat(waitlistRepository.countByCourseId(course.getId())).isEqualTo(2);

        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(updatedCourse.getCurrentEnrollmentCount()).isEqualTo(10);

        executor.shutdown();
    }
}
