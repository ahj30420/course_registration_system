package com.project.course_registration_system.core.course.service;

import static com.project.course_registration_system.common.cache.CacheNames.COURSE_DETAIL;
import static com.project.course_registration_system.common.cache.CacheNames.COURSE_LIST;

import com.project.course_registration_system.common.exception.BaseException;
import com.project.course_registration_system.common.exception.code.CourseErrorCode;
import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.CreateCourseRequest;
import com.project.course_registration_system.core.course.dto.CourseResponse;
import com.project.course_registration_system.core.course.dto.CourseSummaryResponse;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional
    @CacheEvict(cacheNames = COURSE_LIST, allEntries = true)
    public CourseResponse create(CreateCourseRequest request, Long creatorId) {
        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .price(request.price())
                .capacity(request.capacity())
                .creatorId(creatorId)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(CourseStatus.DRAFT)
                .build();
        Course saved = courseRepository.save(course);
        return CourseResponse.from(saved);
    }

    @Cacheable(cacheNames = COURSE_LIST, key = "#status == null ? 'ALL' : #status.name()")
    public List<CourseSummaryResponse> getList(CourseStatus status) {
        return courseRepository.getCourseSummaries(status);
    }

    @Cacheable(cacheNames = COURSE_DETAIL, key = "#courseId")
    public CourseResponse getDetail(Long courseId) {
        Course course = getCourseOrThrow(courseId);
        return CourseResponse.from(course);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = COURSE_LIST, allEntries = true),
            @CacheEvict(cacheNames = COURSE_DETAIL, key = "#courseId")
    })
    public CourseResponse changeStatus(Long courseId, Long creatorId, CourseStatus status) {
        Course course = getCourseOrThrow(courseId);
        validateCourseOwner(course, creatorId);
        course.changeStatus(status);
        return CourseResponse.from(course);
    }

    private Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new BaseException(CourseErrorCode.COURSE_NOT_FOUND));
    }

    private void validateCourseOwner(Course course, Long creatorId) {
        if (!course.isOwner(creatorId)) {
            throw new BaseException(CourseErrorCode.NOT_COURSE_OWNER);
        }
    }
}
