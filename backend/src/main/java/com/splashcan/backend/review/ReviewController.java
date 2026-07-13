package com.splashcan.backend.review;

import com.splashcan.backend.review.dto.CreateReviewRequest;
import com.splashcan.backend.review.dto.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "List reviews for a product")
    @ApiResponse(responseCode = "200", description = "Reviews returned (empty list if the product has none, including an unknown product id)")
    @GetMapping
    public List<ReviewResponse> getReviews(@PathVariable Long productId) {
        return reviewService.findByProduct(productId);
    }

    @Operation(summary = "Create a review for a purchased product")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Review created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "403", description = "User has not purchased this product")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "409", description = "User has already reviewed this product")
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(Authentication authentication, @PathVariable Long productId,
                                                        @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(authentication.getName(), productId, request));
    }
}
