package com.project.course_registration_system.core.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.course_registration_system.common.config.QueryDSLConfig;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.CourseSummaryResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Import({CourseCustomRepositoryImpl.class, QueryDSLConfig.class})
@Sql(scripts = "/init_db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CourseJpaRepositoryTest {

    @Autowired
    private CourseRepository sut;

    @Test
    @DisplayName("전체 강의 요약 조회[status == null]")
    void get_all_course_summaries() throws Exception {
        // given
        CourseStatus status = null;

        // when
        List<CourseSummaryResponse> result = sut.getCourseSummaries(status);

        // then
        assertThat(result).hasSize(10);
    }

    @Test
    @DisplayName("OPEN 상태 강의 목록 조회")
    void get_course_summaries_by_status_open() throws Exception {
        // given
        CourseStatus status = CourseStatus.OPEN;

        // when
        List<CourseSummaryResponse> result = sut.getCourseSummaries(status);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result)
                .allMatch(course -> course.status() == status);
    }

    @Test
    @DisplayName("DRAFT 상태 강의 목록 조회")
    void get_course_summaries_by_status_draft() throws Exception {
        // given
        CourseStatus status = CourseStatus.DRAFT;

        // when
        List<CourseSummaryResponse> result = sut.getCourseSummaries(status);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result)
                .allMatch(course -> course.status() == status);
    }

}