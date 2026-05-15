package com.project.course_registration_system.core.enrollment.service;

import static com.project.course_registration_system.common.cache.CacheNames.COURSE_DETAIL;
import static com.project.course_registration_system.common.cache.CacheNames.COURSE_LIST;

import com.project.course_registration_system.common.exception.BaseException;
import com.project.course_registration_system.common.exception.code.CourseErrorCode;
import com.project.course_registration_system.common.exception.code.EnrollmentErrorCode;
import com.project.course_registration_system.common.exception.code.WaitlistErrorCode;
import com.project.course_registration_system.common.response.PageResponse;
import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import com.project.course_registration_system.core.enrollment.domain.Waitlist;
import com.project.course_registration_system.core.enrollment.dto.CourseEnrollmentResponse;
import com.project.course_registration_system.core.enrollment.dto.EnrollmentResponse;
import com.project.course_registration_system.core.enrollment.dto.MyEnrollmentResponse;
import com.project.course_registration_system.core.enrollment.dto.MyWaitlistRankResponse;
import com.project.course_registration_system.core.enrollment.dto.MyWaitlistResponse;
import com.project.course_registration_system.core.enrollment.repository.EnrollmentRepository;
import com.project.course_registration_system.core.enrollment.repository.WaitlistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Caching(evict = {
            @CacheEvict(cacheNames = COURSE_LIST, allEntries = true),
            @CacheEvict(cacheNames = COURSE_DETAIL, key = "#courseId")
    })
    public EnrollmentResponse enroll(Long courseId, Long userId) {
        validateNotAlreadyEnrolled(courseId, userId);

        Course course = getCourseWithLock(courseId);

        validateCourseOpen(course);

        if (course.hasCapacity()) {
            return enrollToCourse(course, userId);
        }

        return addToWaitlist(course, userId);
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

    private EnrollmentResponse addToWaitlist(Course course, Long userId) {
        Waitlist waitlist = Waitlist.builder()
                .course(course)
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
    @Caching(evict = {
            @CacheEvict(cacheNames = COURSE_LIST, allEntries = true),
            @CacheEvict(cacheNames = COURSE_DETAIL, key = "#result.courseId()", condition = "#result != null")
    })
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
        if (!course.hasCapacity()) {
            return;
        }

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

    public PageResponse<MyEnrollmentResponse> getMyEnrollments(Long userId, Pageable pageable) {
        Page<Enrollment> pageEnrollment = enrollmentRepository.findByUserIdOrderByCreatedAtDesc(userId,
                pageable);
        List<MyEnrollmentResponse> content = pageEnrollment.getContent().stream()
                .map(MyEnrollmentResponse::from).toList();
        return PageResponse.from(pageEnrollment, content);
    }

    public PageResponse<MyWaitlistResponse> getMyWaitlist(Long userId, Pageable pageable) {
        Page<Waitlist> pageWaitlist = waitlistRepository.findByUserIdOrderByCreatedAtDesc(userId,
                pageable);
        List<MyWaitlistResponse> content = pageWaitlist.getContent().stream()
                .map(MyWaitlistResponse::from).toList();
        return PageResponse.from(pageWaitlist, content);
    }

    public PageResponse<CourseEnrollmentResponse> getCourseEnrollments(Long courseId, Long creatorId,
                                                                       Pageable pageable) {
        Course course = getCourseOrThrow(courseId);
        validateCourseOwner(course, creatorId);

        Page<Enrollment> pageEnrollment = enrollmentRepository.findByCourseIdAndStatusInOrderByCreatedAtDesc(
                courseId,
                List.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED),
                pageable
        );
        List<CourseEnrollmentResponse> content = pageEnrollment.getContent().stream()
                .map(CourseEnrollmentResponse::from).toList();
        return PageResponse.from(pageEnrollment, content);
    }

    private Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new BaseException(CourseErrorCode.COURSE_NOT_FOUND));
    }

    public MyWaitlistRankResponse getWaitlistRank(Long waitlistId, Long userId) {
        Waitlist waitlist = getWaitlistOrThrow(waitlistId);

        validateWaitlistOwner(waitlist, userId);

        long aheadCount = waitlistRepository.countByCourseIdAndCreatedAtBefore(
                waitlist.getCourse().getId(),
                waitlist.getCreatedAt()
        );

        return MyWaitlistRankResponse.from(waitlist, aheadCount + 1);
    }

    private Waitlist getWaitlistOrThrow(Long waitlistId) {
        return waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new BaseException(WaitlistErrorCode.WAITLIST_NOT_FOUND));
    }

    private void validateWaitlistOwner(Waitlist waitlist, Long userId) {
        if (!waitlist.isOwner(userId)) {
            throw new BaseException(WaitlistErrorCode.NOT_WAITLIST_OWNER);
        }
    }

    private void validateCourseOwner(Course course, Long creatorId) {
        if (!course.isOwner(creatorId)) {
            throw new BaseException(CourseErrorCode.NOT_COURSE_OWNER);
        }
    }
}
