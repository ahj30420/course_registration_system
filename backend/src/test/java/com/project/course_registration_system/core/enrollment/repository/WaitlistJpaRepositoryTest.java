package com.project.course_registration_system.core.enrollment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.course_registration_system.common.config.QueryDSLConfig;
import com.project.course_registration_system.core.enrollment.domain.Waitlist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Import(QueryDSLConfig.class)
@Sql(scripts = "/init_db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WaitlistJpaRepositoryTest {

    @Autowired
    private WaitlistRepository sut;

    @Test
    @DisplayName("사용자 ID로 대기열을 조회하면 코스 정보와 함께 페이징되어 반환된다")
    void findByUserId_with_fetch_join_success() {
        // given
        Long userId = 1L;
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<Waitlist> result = sut.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(2);

        // Fetch Join 검증 (Lazy Loading 에러가 나지 않아야 함)
        Waitlist firstWaitlist = result.getContent().get(0);
        assertThat(firstWaitlist.getCourse().getTitle()).isEqualTo("JPA 실전");
        assertThat(firstWaitlist.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("대기열이 없는 사용자를 조회하면 빈 페이지가 반환된다")
    void findByUserId_returns_empty_page() {
        // given
        Long nonExistentUserId = 999L;
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<Waitlist> result = sut.findByUserIdOrderByCreatedAtDesc(nonExistentUserId, pageRequest);

        // then
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

}