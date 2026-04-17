package com.project.course_registration_system.core.course.repository.jpa;

import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.response.CourseSummaryResponse;
import java.util.List;

public interface CourseCustomRepository {
    List<CourseSummaryResponse> getCourseSummaries(CourseStatus status);
}
