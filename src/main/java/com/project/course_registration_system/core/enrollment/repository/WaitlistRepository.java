package com.project.course_registration_system.core.enrollment.repository;

import com.project.course_registration_system.core.enrollment.domain.Waitlist;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    long countByCourseId(Long id);

    Optional<Waitlist> findFirstByCourseIdOrderByCreatedAtAsc(Long courseId);

    @Query(
            value = "select distinct w from Waitlist w join fetch w.course where w.userId = :userId",
            countQuery = "select count(w) from Waitlist w where w.userId = :userId"
    )
    Page<Waitlist> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
