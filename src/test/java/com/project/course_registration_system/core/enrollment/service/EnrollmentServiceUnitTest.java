package com.project.course_registration_system.core.enrollment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import com.project.course_registration_system.core.enrollment.repository.EnrollmentRepository;
import com.project.course_registration_system.core.enrollment.repository.WaitlistRepository;
import com.project.course_registration_system.core.fixtures.CourseTestFixture;
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

}
