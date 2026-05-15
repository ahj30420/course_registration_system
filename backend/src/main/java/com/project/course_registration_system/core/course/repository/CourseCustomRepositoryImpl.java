package com.project.course_registration_system.core.course.repository;

import static com.project.course_registration_system.core.course.domain.QCourse.course;

import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.CourseSummaryResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CourseCustomRepositoryImpl implements CourseCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CourseSummaryResponse> getCourseSummaries(CourseStatus status) {
        return queryFactory
                .select(
                        Projections.constructor(
                                CourseSummaryResponse.class,
                                course.id,
                                course.title,
                                course.price,
                                course.status
                        ))
                .from(course)
                .where(statusEq(status))
                .fetch();
    }

    private BooleanExpression statusEq(CourseStatus status) {
        return status != null ? course.status.eq(status) : null;
    }

}
