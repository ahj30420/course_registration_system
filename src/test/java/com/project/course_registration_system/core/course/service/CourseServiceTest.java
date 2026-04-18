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
import com.project.course_registration_system.core.course.dto.request.CreateCourseRequest;
import com.project.course_registration_system.core.course.dto.response.CourseResponse;
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
    @DisplayName("강의 상태 변경 테스트: 성공")
    void change_status_success() throws Exception {
        // given
        Long courseId = 1L;
        Long creatorId = 1L;
        CourseStatus newStatus = CourseStatus.OPEN;

        Course course = CourseTestFixture.create(CourseStatus.DRAFT);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // when
        CourseResponse response = sut.changeStatus(courseId, creatorId, newStatus);

        // then
        assertThat(response.status()).isEqualTo(newStatus);
        assertThat(course.getStatus()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("강의 상태 변경 테스트: 실패[해당 강의에 권한이 없는 경우]")
    void change_status_fail_when_is_not_owner() throws Exception {
        // given
        Long courseId = 1L;
        Long creatorId = 2L;
        CourseStatus newStatus = CourseStatus.OPEN;

        Course course = CourseTestFixture.create(CourseStatus.DRAFT);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));


        // when & then
        assertThatThrownBy(() ->
                sut.changeStatus(courseId, creatorId, CourseStatus.OPEN))
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.NOT_ENROLLMENT_OWNER.getMessage());
    }

    @Test
    @DisplayName("강의 상태 변경 테스트: 실패[강의가 존재하지 않는 경우]")
    void change_status_fail_not_found() throws Exception {
        // given
        Long courseId = -1L;
        Long creatorId = 1L;

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                sut.changeStatus(courseId, creatorId, CourseStatus.OPEN))
                .isInstanceOf(BaseException.class)
                .hasMessage(CourseErrorCode.COURSE_NOT_FOUND.getMessage());
    }
}