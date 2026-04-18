package com.project.course_registration_system.core.enrollment.repository;

import com.project.course_registration_system.core.enrollment.domain.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    long countByCourseId(Long id);
}
