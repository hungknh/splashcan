package com.splashcan.backend.admin;

import com.splashcan.backend.admin.dto.AdminOrderResponse;
import com.splashcan.backend.admin.dto.AdminProductRequest;
import com.splashcan.backend.admin.dto.AdminVariantRequest;
import com.splashcan.backend.admin.dto.DashboardStatsResponse;
import com.splashcan.backend.admin.dto.UpdateOrderStatusRequest;
import com.splashcan.backend.cart.dto.AddCartItemRequest;
import com.splashcan.backend.cart.dto.CartResponse;
import com.splashcan.backend.category.Category;
import com.splashcan.backend.category.CategoryRepository;
import com.splashcan.backend.order.Order;
import com.splashcan.backend.order.dto.CreateOrderRequest;
import com.splashcan.backend.order.dto.OrderResponse;
import com.splashcan.backend.product.dto.ProductResponse;
import com.splashcan.backend.product.dto.ProductVariantResponse;
import com.splashcan.backend.user.User;
import com.splashcan.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void nonAdminCannotAccessAdminEndpoints() {
        String token = registerAndGetToken("admin-test-customer", false);
        HttpHeaders headers = authHeaders(token);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/admin/dashboard/stats", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void adminCanCreateUpdateAndDeleteProductAndVariant() {
        String adminToken = registerAndGetToken("admin-test-crud", true);
        HttpHeaders headers = authHeaders(adminToken);
        Category category = categoryRepository.findAll().get(0);

        AdminProductRequest createRequest = new AdminProductRequest(
                "Admin Test Product", "desc", new BigDecimal("10000"), category.getId(), null, null, true);
        ResponseEntity<ProductResponse> createResponse = restTemplate.exchange(
                "/api/admin/products", HttpMethod.POST, new HttpEntity<>(createRequest, headers), ProductResponse.class);
        assertThat(createResponse.getStatusCode().value()).isEqualTo(201);
        Long productId = createResponse.getBody().id();

        AdminProductRequest updateRequest = new AdminProductRequest(
                "Admin Test Product Updated", "desc2", new BigDecimal("12000"), category.getId(), null, null, true);
        ResponseEntity<ProductResponse> updateResponse = restTemplate.exchange(
                "/api/admin/products/" + productId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), ProductResponse.class);
        assertThat(updateResponse.getBody().name()).isEqualTo("Admin Test Product Updated");

        AdminVariantRequest variantRequest = new AdminVariantRequest(
                "Test Flavor", 330, new BigDecimal("10000"), 50, "ADMIN-TEST-" + UUID.randomUUID());
        ResponseEntity<ProductVariantResponse> variantResponse = restTemplate.exchange(
                "/api/admin/products/" + productId + "/variants", HttpMethod.POST,
                new HttpEntity<>(variantRequest, headers), ProductVariantResponse.class);
        assertThat(variantResponse.getStatusCode().value()).isEqualTo(201);
        Long variantId = variantResponse.getBody().id();

        ResponseEntity<Void> deleteVariantResponse = restTemplate.exchange(
                "/api/admin/variants/" + variantId, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleteVariantResponse.getStatusCode().value()).isEqualTo(204);

        ResponseEntity<Void> deleteProductResponse = restTemplate.exchange(
                "/api/admin/products/" + productId, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleteProductResponse.getStatusCode().value()).isEqualTo(204);
    }

    @Test
    void adminCanTransitionOrderStatusValidlyButNotSkip() {
        String customerToken = registerAndGetToken("admin-test-order-customer", false);
        String adminToken = registerAndGetToken("admin-test-order-admin", true);
        HttpHeaders customerHeaders = authHeaders(customerToken);
        HttpHeaders adminHeaders = authHeaders(adminToken);

        restTemplate.exchange("/api/cart/items", HttpMethod.POST,
                new HttpEntity<>(new AddCartItemRequest(1L, 1), customerHeaders), CartResponse.class);
        ResponseEntity<OrderResponse> orderResponse = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(new CreateOrderRequest("Admin Order Test St"), customerHeaders), OrderResponse.class);
        Long orderId = orderResponse.getBody().id();

        ResponseEntity<String> invalidResponse = restTemplate.exchange(
                "/api/admin/orders/" + orderId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(new UpdateOrderStatusRequest(Order.Status.COMPLETED), adminHeaders), String.class);
        assertThat(invalidResponse.getStatusCode().value()).isEqualTo(400);

        ResponseEntity<AdminOrderResponse> validResponse = restTemplate.exchange(
                "/api/admin/orders/" + orderId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(new UpdateOrderStatusRequest(Order.Status.PAID), adminHeaders), AdminOrderResponse.class);
        assertThat(validResponse.getBody().status()).isEqualTo(Order.Status.PAID);
    }

    @Test
    void dashboardStatsMatchManualCalculation() {
        String adminToken = registerAndGetToken("admin-test-dashboard", true);
        HttpHeaders adminHeaders = authHeaders(adminToken);

        ResponseEntity<DashboardStatsResponse> before = restTemplate.exchange(
                "/api/admin/dashboard/stats", HttpMethod.GET, new HttpEntity<>(adminHeaders), DashboardStatsResponse.class);
        BigDecimal revenueBefore = before.getBody().totalRevenue();
        long ordersBefore = before.getBody().totalOrders();

        String customerToken = registerAndGetToken("admin-test-dashboard-customer", false);
        HttpHeaders customerHeaders = authHeaders(customerToken);
        restTemplate.exchange("/api/cart/items", HttpMethod.POST,
                new HttpEntity<>(new AddCartItemRequest(2L, 1), customerHeaders), CartResponse.class);
        ResponseEntity<OrderResponse> orderResponse = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(new CreateOrderRequest("Dashboard Test St"), customerHeaders), OrderResponse.class);
        BigDecimal orderTotal = orderResponse.getBody().totalAmount();
        Long orderId = orderResponse.getBody().id();

        restTemplate.exchange("/api/orders/" + orderId + "/pay", HttpMethod.POST,
                new HttpEntity<>(customerHeaders), String.class);

        ResponseEntity<DashboardStatsResponse> after = restTemplate.exchange(
                "/api/admin/dashboard/stats", HttpMethod.GET, new HttpEntity<>(adminHeaders), DashboardStatsResponse.class);

        assertThat(after.getBody().totalRevenue()).isEqualByComparingTo(revenueBefore.add(orderTotal));
        assertThat(after.getBody().totalOrders()).isEqualTo(ordersBefore + 1);
    }

    @SuppressWarnings("unchecked")
    private String registerAndGetToken(String emailPrefix, boolean promoteToAdmin) {
        String email = emailPrefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        Map<String, String> body = Map.of("email", email, "password", "password123", "fullName", "Admin Tester");
        restTemplate.postForEntity("/api/auth/register", body, Map.class);

        if (promoteToAdmin) {
            User user = userRepository.findByEmail(email).orElseThrow();
            user.setRole(User.Role.ADMIN);
            userRepository.save(user);
        }

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/api/auth/login", Map.of("email", email, "password", "password123"), Map.class);
        return (String) loginResponse.getBody().get("accessToken");
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}
