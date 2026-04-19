package com.project.course_registration_system.core.enrollment.domain;

import com.project.course_registration_system.common.audting.BaseTimeEntity;
import com.project.course_registration_system.core.course.domain.Course;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder
    private Waitlist(Course course, Long userId) {
        this.course = course;
        this.userId = userId;
    }
}
