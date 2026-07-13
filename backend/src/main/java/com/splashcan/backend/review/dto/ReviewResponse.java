package com.splashcan.backend.review.dto;

import com.splashcan.backend.review.Review;

import java.time.OffsetDateTime;

public record ReviewResponse(
        Long id,
        String userFullName,
        Integer rating,
        String comment,
        OffsetDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getFullName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
