package com.splashcan.backend.review;

import com.splashcan.backend.exception.AlreadyReviewedException;
import com.splashcan.backend.exception.NotPurchasedException;
import com.splashcan.backend.exception.ProductNotFoundException;
import com.splashcan.backend.order.Order;
import com.splashcan.backend.order.OrderItemRepository;
import com.splashcan.backend.product.ProductRepository;
import com.splashcan.backend.review.dto.CreateReviewRequest;
import com.splashcan.backend.review.dto.ReviewResponse;
import com.splashcan.backend.user.User;
import com.splashcan.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final List<Order.Status> PURCHASED_STATUSES =
            List.of(Order.Status.PAID, Order.Status.SHIPPED, Order.Status.COMPLETED);

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponse> findByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public ReviewResponse createReview(String email, Long productId, CreateReviewRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        if (!orderItemRepository.existsPurchaseByUserAndProduct(user, productId, PURCHASED_STATUSES)) {
            throw new NotPurchasedException();
        }
        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new AlreadyReviewedException();
        }

        Review review = Review.builder()
                .product(productRepository.getReferenceById(productId))
                .user(user)
                .rating(request.rating())
                .comment(request.comment())
                .build();
        reviewRepository.save(review);
        return ReviewResponse.from(review);
    }
}
