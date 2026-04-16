package com.project.course_registration_system.core.course.repository;

import com.project.course_registration_system.core.course.domain.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CourseRepository {

    private final CourseJpaRepository courseJpaRepository;

    public Course save(Course course) {
        return courseJpaRepository.save(course);
    }

}
