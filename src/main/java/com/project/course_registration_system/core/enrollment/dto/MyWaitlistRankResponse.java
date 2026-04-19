package com.project.course_registration_system.core.enrollment.dto;

import com.project.course_registration_system.core.enrollment.domain.Waitlist;

public record MyWaitlistRankResponse(
        Long waitlistId,
        Long courseId,
        Long userId,
        Long rank
) {
    public static MyWaitlistRankResponse from(Waitlist waitlist, Long rank) {
        return new MyWaitlistRankResponse(
                waitlist.getId(),
                waitlist.getCourse().getId(),
                waitlist.getUserId(),
                rank
        );
    }
}
