package com.project.course_registration_system.core.course.repository;

import com.project.course_registration_system.core.course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long>, CourseCustomRepository {
}
