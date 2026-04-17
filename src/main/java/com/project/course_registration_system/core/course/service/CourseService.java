package com.project.course_registration_system.core.course.service;

import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.response.CourseResponse;
import com.project.course_registration_system.core.course.dto.request.CreateCourseRequest;
import com.project.course_registration_system.core.course.dto.response.CourseSummaryResponse;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional
    public CourseResponse create(CreateCourseRequest request, Long creatorId)  {
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
        return CourseResponse.from(saved, 0L);
    }

    public List<CourseSummaryResponse> getList(CourseStatus status) {
        return courseRepository.getCourseSummaries(status);
    }
}
