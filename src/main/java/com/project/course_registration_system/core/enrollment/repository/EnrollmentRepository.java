package com.project.course_registration_system.core.enrollment.repository;

import com.project.course_registration_system.core.enrollment.domain.Enrollment;
import com.project.course_registration_system.core.enrollment.domain.EnrollmentStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByCourseIdAndUserIdAndStatusIn(Long courseId, Long userId, List<EnrollmentStatus> statuses);

    long countByCourseId(Long id);

    long countByCourseIdAndStatusIn(Long courseId, List<EnrollmentStatus> statuses);

    @Query(
            value = "select distinct e from Enrollment e join fetch e.course where e.userId = :userId",
            countQuery = "select count(e) from Enrollment e where e.userId = :userId"
    )
    Page<Enrollment> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query(
            value = "select e from Enrollment e where e.course.id = :courseId and e.status in :statuses order by e.createdAt desc"
    )
    Page<Enrollment> findByCourseIdAndStatusInOrderByCreatedAtDesc(
            @Param("courseId") Long courseId,
            @Param("statuses") List<EnrollmentStatus> statuses,
            Pageable pageable
    );
}
