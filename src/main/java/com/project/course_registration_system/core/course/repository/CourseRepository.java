package com.project.course_registration_system.core.course.repository;

import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.response.CourseSummaryResponse;
import com.project.course_registration_system.core.course.repository.jpa.CourseJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CourseRepository {

    private final CourseJpaRepository courseJpaRepository;

    public Course save(Course course) {
        return courseJpaRepository.save(course);
    }

    public List<CourseSummaryResponse> getCourseSummaries(CourseStatus status) { return courseJpaRepository.getCourseSummaries(status); }

}
