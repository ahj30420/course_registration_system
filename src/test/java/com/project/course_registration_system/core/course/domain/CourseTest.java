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
    @DisplayName("강의 등록 테스트: 실패[가격이 마이너스일 경우]")
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
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .status(CourseStatus.DRAFT)
                        .build())
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.INVALID_COURSE_PRICE.getMessage());
    }

    @Test
    @DisplayName("강의 등록 테스트: 실패[정원이 마이너스일 경우]")
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
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .status(CourseStatus.DRAFT)
                        .build())
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.INVALID_COURSE_CAPACITY.getMessage());
    }

    @Test
    @DisplayName("강의 상태 변경 테스트: 성공")
    void change_status_success() throws Exception {
        // given
        CourseStatus newStatus = CourseStatus.OPEN;

        Course course = new Course().builder()
                .title("테스트 강의")
                .description("설명")
                .price(1000L)
                .capacity(10)
                .creatorId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(CourseStatus.DRAFT)
                .build();

        // when
        course.changeStatus(newStatus);

        // then
        assertThat(course.getStatus()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("수강 신청 가능 여부 확인: 정원이 남았을 때 성공")
    void hasCapacity_success() throws Exception {
        // given
        Course course = new Course().builder()
                .title("테스트 강의")
                .description("설명")
                .price(1000L)
                .capacity(10)
                .creatorId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(CourseStatus.DRAFT)
                .build();

        // when & then
        assertThat(course.hasCapacity()).isTrue();
    }

    @Test
    @DisplayName("인원수 증가 테스트: 성공")
    void incrementEnrollmentCount_success() {
        // given
        Course course = Course.builder()
                .capacity(10)
                .price(1000L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(CourseStatus.OPEN)
                .build();

        // when
        course.incrementEnrollmentCount();

        // then
        assertThat(course.getCurrentEnrollmentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("인원수 증가 테스트: 실패[정원 초과 시 예외 발생]")
    void incrementEnrollmentCount_fail_overflow() {
        // given
        Course course = Course.builder()
                .capacity(1)
                .price(1000L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(CourseStatus.OPEN)
                .build();
        course.incrementEnrollmentCount();

        // when & then
        assertThatThrownBy(course::incrementEnrollmentCount)
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.COURSE_CAPACITY_EXCEEDED.getMessage());
    }


    @Test
    @DisplayName("수강 신청 가능 여부 확인: 정원이 가득 찼을 때 실패")
    void hasCapacity_fail_when_full() {
        // given
        Course course = Course.builder()
                .capacity(1)
                .price(1000L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(CourseStatus.OPEN)
                .build();

        course.incrementEnrollmentCount();

        // when & then
        assertThat(course.hasCapacity()).isFalse();
    }

}