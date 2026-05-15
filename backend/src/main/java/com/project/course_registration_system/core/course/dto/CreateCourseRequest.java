package com.project.course_registration_system.core.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateCourseRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull @Min(0) Long price,
        @NotNull @Min(1) Integer capacity,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
