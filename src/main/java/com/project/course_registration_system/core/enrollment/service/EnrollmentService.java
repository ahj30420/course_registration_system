package com.project.course_registration_system.core.enrollment.service;

import com.project.course_registration_system.common.exception.BaseException;
import com.project.course_registration_system.common.exception.code.CourseErrorCode;
import com.project.course_registration_system.common.exception.code.EnrollmentErrorCode;
import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import com.project.course_registration_system.core.enrollment.domain.Waitlist;
import com.project.course_registration_system.core.enrollment.dto.EnrollmentResponse;
import com.project.course_registration_system.core.enrollment.repository.EnrollmentRepository;
import com.project.course_registration_system.core.enrollment.repository.WaitlistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final WaitlistRepository waitlistRepository;

    @Value("${enrollment.cancellation-days:7}")
    private int cancellationDays;

    @Transactional
    public EnrollmentResponse enroll(Long courseId, Long userId) {
        validateNotAlreadyEnrolled(courseId, userId);

        Course course = getCourseWithLock(courseId);

        validateCourseOpen(course);

        if (course.hasCapacity()) {
            return enrollToCourse(course, userId);
        }

        return addToWaitlist(courseId, userId);
    }

    private void validateNotAlreadyEnrolled(Long courseId, Long userId) {
        boolean alreadyEnrolled = enrollmentRepository.existsByCourseIdAndUserIdAndStatusIn(
                courseId, userId, List.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED));

        if (alreadyEnrolled) {
            throw new BaseException(EnrollmentErrorCode.ALREADY_ENROLLED);
        }
    }

    private Course getCourseWithLock(Long courseId) {
        return courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() -> new BaseException(CourseErrorCode.COURSE_NOT_FOUND));
    }

    private void validateCourseOpen(Course course) {
        if (!course.isOpen()) {
            throw new BaseException(EnrollmentErrorCode.COURSE_NOT_OPEN);
        }
    }

    private EnrollmentResponse enrollToCourse(Course course, Long userId) {
        course.incrementEnrollmentCount();

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .userId(userId)
                .status(EnrollmentStatus.PENDING)
                .build();

        enrollmentRepository.save(enrollment);
        return EnrollmentResponse.from(enrollment);
    }

    private EnrollmentResponse addToWaitlist(Long courseId, Long userId) {
        Waitlist waitlist = Waitlist.builder()
                .courseId(courseId)
                .userId(userId)
                .build();

        waitlistRepository.save(waitlist);
        return EnrollmentResponse.from(waitlist);
    }

    @Transactional
    public EnrollmentResponse confirm(Long enrollmentId, Long userId) {
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);
        validateEnrollmentOwner(enrollment, userId);
        enrollment.confirm();
        return EnrollmentResponse.from(enrollment);
    }

    private Enrollment getEnrollmentOrThrow(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BaseException(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND));
    }

    private void validateEnrollmentOwner(Enrollment enrollment, Long userId) {
        if (!enrollment.isOwner(userId)) {
            throw new BaseException(EnrollmentErrorCode.NOT_ENROLLMENT_OWNER);
        }
    }

    @Transactional
    public EnrollmentResponse cancel(Long enrollmentId, Long userId) {
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);

        validateEnrollmentOwner(enrollment, userId);

        boolean wasActive = enrollment.isActive();

        enrollment.cancel(cancellationDays);

        if (wasActive) {
            Course course = courseRepository.findByIdForUpdate(enrollment.getCourse().getId())
                    .orElseThrow(() -> new BaseException(CourseErrorCode.COURSE_NOT_FOUND));
            course.decrementEnrollmentCount();
            promoteFromWaitlist(course);
        }

        return EnrollmentResponse.from(enrollment);
    }

    private void promoteFromWaitlist(Course course) {
        if (!course.hasCapacity()) return;

        waitlistRepository.findFirstByCourseIdOrderByCreatedAtAsc(course.getId())
                .ifPresent(waitlist -> {
                   waitlistRepository.delete(waitlist);
                   course.incrementEnrollmentCount();
                   Enrollment promoted = Enrollment.builder()
                           .course(course)
                           .userId(waitlist.getUserId())
                           .status(EnrollmentStatus.PENDING)
                           .build();
                   enrollmentRepository.save(promoted);
                });
    }
}
