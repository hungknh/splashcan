package com.splashcan.backend.review;

import com.splashcan.backend.cart.dto.AddCartItemRequest;
import com.splashcan.backend.cart.dto.CartResponse;
import com.splashcan.backend.order.dto.CreateOrderRequest;
import com.splashcan.backend.order.dto.OrderResponse;
import com.splashcan.backend.product.ProductVariant;
import com.splashcan.backend.product.ProductVariantRepository;
import com.splashcan.backend.review.dto.CreateReviewRequest;
import com.splashcan.backend.review.dto.ReviewResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReviewControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Test
    void userWithoutPurchaseCannotReview() {
        String token = registerAndGetToken("review-no-purchase");
        HttpHeaders headers = authHeaders(token);
        ProductVariant variant = productVariantRepository.findAll().get(0);
        Long productId = variant.getProduct().getId();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products/" + productId + "/reviews", HttpMethod.POST,
                new HttpEntity<>(new CreateReviewRequest(5, "Great!"), headers), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void userWhoPurchasedAndPaidCanReview() {
        String token = registerAndGetToken("review-purchaser");
        HttpHeaders headers = authHeaders(token);
        ProductVariant variant = productVariantRepository.findAll().get(1);
        Long productId = variant.getProduct().getId();

        restTemplate.exchange("/api/cart/items", HttpMethod.POST,
                new HttpEntity<>(new AddCartItemRequest(variant.getId(), 1), headers), CartResponse.class);
        ResponseEntity<OrderResponse> orderResponse = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(new CreateOrderRequest("Review Test St"), headers), OrderResponse.class);
        Long orderId = orderResponse.getBody().id();
        restTemplate.exchange("/api/orders/" + orderId + "/pay", HttpMethod.POST,
                new HttpEntity<>(headers), String.class);

        ResponseEntity<ReviewResponse> response = restTemplate.exchange(
                "/api/products/" + productId + "/reviews", HttpMethod.POST,
                new HttpEntity<>(new CreateReviewRequest(5, "Great product!"), headers), ReviewResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody().rating()).isEqualTo(5);

        ResponseEntity<List> listResponse = restTemplate.getForEntity(
                "/api/products/" + productId + "/reviews", List.class);
        assertThat(listResponse.getBody()).isNotEmpty();
    }

    @Test
    void purchasedUserCannotReviewTwice() {
        String token = registerAndGetToken("review-double");
        HttpHeaders headers = authHeaders(token);
        ProductVariant variant = productVariantRepository.findAll().get(2);
        Long productId = variant.getProduct().getId();

        restTemplate.exchange("/api/cart/items", HttpMethod.POST,
                new HttpEntity<>(new AddCartItemRequest(variant.getId(), 1), headers), CartResponse.class);
        ResponseEntity<OrderResponse> orderResponse = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(new CreateOrderRequest("Review Double Test St"), headers), OrderResponse.class);
        Long orderId = orderResponse.getBody().id();
        restTemplate.exchange("/api/orders/" + orderId + "/pay", HttpMethod.POST,
                new HttpEntity<>(headers), String.class);

        restTemplate.exchange("/api/products/" + productId + "/reviews", HttpMethod.POST,
                new HttpEntity<>(new CreateReviewRequest(4, "First review"), headers), ReviewResponse.class);

        ResponseEntity<String> secondResponse = restTemplate.exchange(
                "/api/products/" + productId + "/reviews", HttpMethod.POST,
                new HttpEntity<>(new CreateReviewRequest(3, "Second review"), headers), String.class);

        assertThat(secondResponse.getStatusCode().value()).isEqualTo(409);
    }

    @SuppressWarnings("unchecked")
    private String registerAndGetToken(String emailPrefix) {
        Map<String, String> body = Map.of(
                "email", emailPrefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "password", "password123",
                "fullName", "Review Tester");
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/auth/register", body, Map.class);
        return (String) response.getBody().get("accessToken");
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}
