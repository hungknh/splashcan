package com.splashcan.backend.order;

import com.splashcan.backend.cart.dto.AddCartItemRequest;
import com.splashcan.backend.cart.dto.CartResponse;
import com.splashcan.backend.order.dto.CreateOrderRequest;
import com.splashcan.backend.order.dto.OrderResponse;
import com.splashcan.backend.order.dto.PayOrderRequest;
import com.splashcan.backend.product.ProductVariant;
import com.splashcan.backend.product.ProductVariantRepository;
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
class OrderControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Test
    void createOrderDecreasesStockAndPaySucceeds() {
        String token = registerAndGetToken("order-user1");
        ProductVariant variant = productVariantRepository.findAll().get(0);
        int stockBefore = variant.getStockQuantity();
        HttpHeaders headers = authHeaders(token);

        restTemplate.exchange("/api/cart/items", HttpMethod.POST,
                new HttpEntity<>(new AddCartItemRequest(variant.getId(), 2), headers), CartResponse.class);

        ResponseEntity<OrderResponse> orderResponse = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(new CreateOrderRequest("123 Test Street"), headers),
                OrderResponse.class);
        assertThat(orderResponse.getStatusCode().value()).isEqualTo(201);
        assertThat(orderResponse.getBody().status()).isEqualTo(Order.Status.PENDING);
        assertThat(orderResponse.getBody().items()).hasSize(1);

        ProductVariant variantAfter = productVariantRepository.findById(variant.getId()).orElseThrow();
        assertThat(variantAfter.getStockQuantity()).isEqualTo(stockBefore - 2);

        Long orderId = orderResponse.getBody().id();
        ResponseEntity<OrderResponse> payResponse = restTemplate.exchange(
                "/api/orders/" + orderId + "/pay", HttpMethod.POST,
                new HttpEntity<>(headers), OrderResponse.class);
        assertThat(payResponse.getBody().status()).isEqualTo(Order.Status.PAID);
    }

    @Test
    void createOrderWithEmptyCartReturns400() {
        String token = registerAndGetToken("order-user2");
        HttpHeaders headers = authHeaders(token);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(new CreateOrderRequest("456 Empty Street"), headers),
                String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void paymentFailureKeepsOrderPending() {
        String token = registerAndGetToken("order-user3");
        ProductVariant variant = productVariantRepository.findAll().get(1);
        HttpHeaders headers = authHeaders(token);

        restTemplate.exchange("/api/cart/items", HttpMethod.POST,
                new HttpEntity<>(new AddCartItemRequest(variant.getId(), 1), headers), CartResponse.class);
        ResponseEntity<OrderResponse> orderResponse = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(new CreateOrderRequest("789 Fail Street"), headers),
                OrderResponse.class);
        Long orderId = orderResponse.getBody().id();

        ResponseEntity<OrderResponse> payResponse = restTemplate.exchange(
                "/api/orders/" + orderId + "/pay", HttpMethod.POST,
                new HttpEntity<>(new PayOrderRequest(false), headers), OrderResponse.class);

        assertThat(payResponse.getBody().status()).isEqualTo(Order.Status.PENDING);
    }

    @Test
    void userCannotViewAnotherUsersOrder() {
        String tokenA = registerAndGetToken("order-userA");
        String tokenB = registerAndGetToken("order-userB");
        ProductVariant variant = productVariantRepository.findAll().get(2);

        restTemplate.exchange("/api/cart/items", HttpMethod.POST,
                new HttpEntity<>(new AddCartItemRequest(variant.getId(), 1), authHeaders(tokenA)), CartResponse.class);
        ResponseEntity<OrderResponse> orderResponse = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(new CreateOrderRequest("Owner Street"), authHeaders(tokenA)),
                OrderResponse.class);
        Long orderId = orderResponse.getBody().id();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders/" + orderId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(tokenB)), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);

        ResponseEntity<List> listB = restTemplate.exchange(
                "/api/orders", HttpMethod.GET, new HttpEntity<>(authHeaders(tokenB)), List.class);
        assertThat(listB.getBody()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private String registerAndGetToken(String emailPrefix) {
        Map<String, String> body = Map.of(
                "email", emailPrefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "password", "password123",
                "fullName", "Order Tester");
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
