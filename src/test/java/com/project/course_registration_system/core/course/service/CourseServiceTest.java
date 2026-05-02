package com.project.course_registration_system.core.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.course_registration_system.common.exception.BaseException;
import com.project.course_registration_system.common.exception.code.CourseErrorCode;
import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.CreateCourseRequest;
import com.project.course_registration_system.core.course.dto.CourseResponse;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import com.project.course_registration_system.core.fixtures.CourseTestFixture;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @InjectMocks
    private CourseService sut;

    @Mock
    private CourseRepository courseRepository;

    @Test
    @DisplayName("강의 등록 테스트: 성공")
    void create_success() {
        // given
        Long creatorId = 1L;

        CreateCourseRequest request = new CreateCourseRequest(
                "테스트 강의",
                "설명",
                1000L,
                10,
                LocalDate.of(2026, 4, 16),
                LocalDate.of(2026, 4, 21)
        );

        Course savedCourse = Course.builder()
                .title(request.title())
                .description(request.description())
                .price(request.price())
                .capacity(request.capacity())
                .creatorId(creatorId)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(CourseStatus.DRAFT)
                .build();

        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        // when
        CourseResponse result = sut.create(request, creatorId);

        // then
        assertThat(result.title()).isEqualTo("테스트 강의");
        assertThat(result.price()).isEqualTo(1000L);
        assertThat(result.capacity()).isEqualTo(10);
        assertThat(result.creatorId()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(CourseStatus.DRAFT);

        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("강의 상세 조회 테스트: 성공")
    void get_detail_succecss() throws Exception {
        // given
        Long courseId = 1L;

        Course course = CourseTestFixture.create(CourseStatus.DRAFT);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // when
        CourseResponse response = sut.getDetail(courseId);

        // then
        assertThat(response.status()).isNotNull();
    }

    @Test
    @DisplayName("강의 상세 조회 테스트: 실패[강의가 존재하지 않는 경우]")
    void get_detail_fail_not_found() throws Exception {
        // given
        Long courseId = -1L;

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.getDetail(courseId))
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.COURSE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("강의 오픈 테스트: 성공")
    void open_success() {
        // given
        Long courseId = 1L;
        Long creatorId = 1L;
        Course course = CourseTestFixture.create(CourseStatus.DRAFT);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // when
        CourseResponse response = sut.open(courseId, creatorId);

        // then
        assertThat(response.status()).isEqualTo(CourseStatus.OPEN);
        assertThat(course.getStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    @DisplayName("강의 오픈 테스트: 실패[작성자가 아닌 경우]")
    void open_fail_not_owner() {
        // given
        Long courseId = 1L;
        Long strangerId = 999L;
        Course course = CourseTestFixture.create(CourseStatus.DRAFT);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // when & then
        assertThatThrownBy(() -> sut.open(courseId, strangerId))
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.NOT_COURSE_OWNER.getMessage());
    }

    @Test
    @DisplayName("강의 오픈 테스트: 실패[DRAFT 상태가 아닌 경우]")
    void open_fail_invalid_status() {
        // given
        Long courseId = 1L;
        Long creatorId = 1L;

        Course course = CourseTestFixture.create(CourseStatus.CLOSED);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // when & then
        assertThatThrownBy(() -> sut.open(courseId, creatorId))
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.CANNOT_OPEN_COURSE.getMessage());
    }

    @Test
    @DisplayName("강의 마감 테스트: 성공")
    void close_success() {
        // given
        Long courseId = 1L;
        Long creatorId = 1L;
        Course course = CourseTestFixture.create(CourseStatus.OPEN);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // when
        CourseResponse response = sut.close(courseId, creatorId);

        // then
        assertThat(response.status()).isEqualTo(CourseStatus.CLOSED);
        assertThat(course.getStatus()).isEqualTo(CourseStatus.CLOSED);
    }

    @Test
    @DisplayName("강의 마감 테스트: 실패[강의가 존재하지 않는 경우]")
    void close_fail_not_found() {
        // given
        Long courseId = -1L;
        Long creatorId = 1L;

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.close(courseId, creatorId))
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.COURSE_NOT_FOUND.getMessage());
    }
}