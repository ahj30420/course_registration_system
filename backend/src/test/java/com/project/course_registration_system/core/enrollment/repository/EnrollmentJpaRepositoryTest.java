package com.project.course_registration_system.core.enrollment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.course_registration_system.common.config.QueryDSLConfig;
import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Import(QueryDSLConfig.class)
@Sql(scripts = "/init_db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class EnrollmentJpaRepositoryTest {

    @Autowired
    private EnrollmentRepository sut;
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    @DisplayName("내 수강 신청 목록 조회 테스트: 페이징 및 페치 조인 확인")
    void findByUserId_with_paging_and_fetch_join() {
        // given
        Long userId = 1L;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<Enrollment> result = sut.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent().get(0).getCourse().getTitle()).isEqualTo("데이터베이스");
        assertThat(Hibernate.isInitialized(result.getContent().get(0).getCourse())).isTrue();
    }

    @Test
    @DisplayName("내 수강 신청 목록 조회 테스트: 빈 결과 확인")
    void findByUserId_returns_empty_page_when_no_data() {
        // given
        Long nonExistentUserId = 999L;
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<Enrollment> result = sut.findByUserIdOrderByCreatedAtDesc(nonExistentUserId, pageRequest);

        // then
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("강의별 수강생 목록 조회 테스트: 페이징 확인")
    void findByCourseId_with_paging() throws Exception {
        // given
        Long courseId = 1L;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<EnrollmentStatus> statusList = List.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED);

        // when
        Page<Enrollment> result = enrollmentRepository.findByCourseIdAndStatusInOrderByCreatedAtDesc(
                courseId, statusList, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("강의별 수강생 목록 조회 테스트: 빈 결과 확인")
    void findByCourseId_returns_empty_page_when_no_data() throws Exception {
        // given
        Long courseId = 999L;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<EnrollmentStatus> statusList = List.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED);

        // when
        Page<Enrollment> result = enrollmentRepository.findByCourseIdAndStatusInOrderByCreatedAtDesc(
                courseId, statusList, pageRequest);

        // then
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
    }
}