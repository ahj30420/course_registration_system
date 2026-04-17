package com.project.course_registration_system.core.course.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.project.course_registration_system.common.exception.BaseException;
import com.project.course_registration_system.common.exception.code.CourseErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class CourseTest {

    @Test
    @DisplayName("강의 등록 테스트: 성공")
    void create_course_success() throws Exception {
        // given
        LocalDate startDate = LocalDate.of(2026, 4, 16);
        LocalDate endDate = LocalDate.of(2026, 4, 21);

        // when
        Course course = new Course().builder()
                .title("테스트 강의")
                .description("설명")
                .price(1000L)
                .capacity(10)
                .creatorId(1L)
                .startDate(startDate)
                .endDate(endDate)
                .status(CourseStatus.DRAFT)
                .build();

        // then
        assertThat(course.getTitle()).isEqualTo("테스트 강의");
        assertThat(course.getCapacity()).isEqualTo(10);
        assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
    }

    @Test
    @DisplayName("강의 등록 테스트: 실패[시작일이 종료일보다 늦은 경우]")
    void create_course_fail_invalidDate() throws Exception {
        // given
        LocalDate startDate = LocalDate.of(2026, 4, 17);
        LocalDate endDate = LocalDate.of(2026, 4, 16);

        // when & then
        assertThatThrownBy(() ->
                Course.builder()
                        .title("테스트 강의")
                        .description("설명")
                        .price(1000L)
                        .capacity(10)
                        .creatorId(1L)
                        .startDate(startDate)
                        .endDate(endDate)
                        .status(CourseStatus.DRAFT)
                        .build())
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.INVALID_COURSE_PERIOD.getMessage());
    }

    @Test
    void create_course_fail_invalidPrice() throws Exception {
        // given
        Long minusPrice = -1L;

        // when & then
        assertThatThrownBy(() ->
                Course.builder()
                        .title("테스트 강의")
                        .description("설명")
                        .price(minusPrice)
                        .capacity(10)
                        .creatorId(1L)
                        .startDate(LocalDate.of(2026, 4, 16))
                        .endDate(LocalDate.of(2026, 4, 21))
                        .status(CourseStatus.DRAFT)
                        .build())
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.INVALID_COURSE_PRICE.getMessage());
    }

    @Test
    void create_course_fail_invalidCapacity() throws Exception {
        // given
        int minusCapacity = -1;

        // when & then
        assertThatThrownBy(() ->
                Course.builder()
                        .title("테스트 강의")
                        .description("설명")
                        .price(1000L)
                        .capacity(minusCapacity)
                        .creatorId(1L)
                        .startDate(LocalDate.of(2026, 4, 16))
                        .endDate(LocalDate.of(2026, 4, 21))
                        .status(CourseStatus.DRAFT)
                        .build())
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.INVALID_COURSE_CAPACITY.getMessage());
    }

}