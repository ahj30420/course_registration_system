package com.project.course_registration_system.core.enrollment.domain;

import com.project.course_registration_system.common.audting.BaseTimeEntity;
import com.project.course_registration_system.common.exception.BaseException;
import com.project.course_registration_system.common.exception.code.EnrollmentErrorCode;
import com.project.course_registration_system.core.course.domain.Course;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "enrollments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    private LocalDateTime confirmAt;

    private LocalDateTime cancelAt;

    @Builder
    private Enrollment(Course course, Long userId, EnrollmentStatus status) {
        this.course = course;
        this.userId = userId;
        this.status = status;
    }

    public void confirm() {
        if (this.status != EnrollmentStatus.PENDING) {
            throw new BaseException(EnrollmentErrorCode.ENROLLMENT_NOT_PENDING);
        }
        this.confirmAt = LocalDateTime.now();
        this.status = EnrollmentStatus.CONFIRMED;
    }

    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isActive() {
        return this.status == EnrollmentStatus.PENDING || this.status == EnrollmentStatus.CONFIRMED;
    }

    public void cancel(int cancellationDays) {
        if (this.status == EnrollmentStatus.CANCELLED) {
            throw new BaseException(EnrollmentErrorCode.ENROLLMENT_NOT_CANCELLABLE);
        }
        if (this.status == EnrollmentStatus.CONFIRMED) {
            if (confirmAt == null || LocalDateTime.now().isAfter(confirmAt.plusDays(cancellationDays))) {
                throw new BaseException(EnrollmentErrorCode.CANCELLATION_PERIOD_EXPIRED);
            }
        }
        this.status = EnrollmentStatus.CANCELLED;
        this.cancelAt = LocalDateTime.now();
    }
}
