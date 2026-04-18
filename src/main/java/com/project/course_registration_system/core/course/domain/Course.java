package com.project.course_registration_system.core.course.domain;


import com.project.course_registration_system.common.audting.BaseTimeEntity;
import com.project.course_registration_system.common.exception.BaseException;
import com.project.course_registration_system.common.exception.code.CourseErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private int currentEnrollmentCount;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status;

    @Builder
    private Course(
            String title,
            String description,
            Long price,
            Integer capacity,
            Long creatorId,
            LocalDate startDate,
            LocalDate endDate,
            CourseStatus status
    ) {
        if (startDate.isAfter(endDate)) {
            throw new BaseException(CourseErrorCode.INVALID_COURSE_PERIOD);
        }
        if (price < 0) {
            throw new BaseException(CourseErrorCode.INVALID_COURSE_PRICE);
        }
        if (capacity <= 0) {
            throw new BaseException(CourseErrorCode.INVALID_COURSE_CAPACITY);
        }

        this.title = title;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.creatorId = creatorId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.currentEnrollmentCount = 0;
    }

    public void changeStatus(CourseStatus nextStatus) {
        if (nextStatus != null) {
            this.status = nextStatus;
        }
    }

    public boolean isOpen() {
        return this.status == CourseStatus.OPEN;
    }

    public boolean hasCapacity() {
        return this.capacity > this.currentEnrollmentCount;
    }

    public void incrementEnrollmentCount() {
        if (!hasCapacity()) {
            throw new BaseException(CourseErrorCode.COURSE_CAPACITY_EXCEEDED);
        }
        this.currentEnrollmentCount++;
    }
}
