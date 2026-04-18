package com.project.course_registration_system.core.enrollment.domain;

import com.project.course_registration_system.common.audting.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "waitlists",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waitlist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder
    private Waitlist(Long courseId, Long userId) {
        this.courseId = courseId;
        this.userId = userId;
    }
}
