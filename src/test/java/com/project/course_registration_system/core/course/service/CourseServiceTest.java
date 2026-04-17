package com.project.course_registration_system.core.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.course_registration_system.core.course.domain.Course;
import com.project.course_registration_system.core.course.domain.CourseStatus;
import com.project.course_registration_system.core.course.dto.response.CourseResponse;
import com.project.course_registration_system.core.course.dto.request.CreateCourseRequest;
import com.project.course_registration_system.core.course.repository.CourseRepository;
import java.time.LocalDate;
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

}