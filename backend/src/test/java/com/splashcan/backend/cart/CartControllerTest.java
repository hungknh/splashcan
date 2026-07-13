package com.splashcan.backend.cart;

import com.splashcan.backend.cart.dto.AddCartItemRequest;
import com.splashcan.backend.cart.dto.CartResponse;
import com.splashcan.backend.cart.dto.UpdateCartItemRequest;
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

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CartControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Test
    void addUpdateAndRemoveCartItem() {
        String token = registerAndGetToken("cart-user1");
        ProductVariant variant = productVariantRepository.findAll().get(0);
        HttpHeaders headers = authHeaders(token);

        ResponseEntity<CartResponse> addResponse = restTemplate.exchange(
                "/api/cart/items", HttpMethod.POST,
                new HttpEntity<>(new AddCartItemRequest(variant.getId(), 2), headers),
                CartResponse.class);
        assertThat(addResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(addResponse.getBody().items()).hasSize(1);
        assertThat(addResponse.getBody().items().get(0).quantity()).isEqualTo(2);

        Long itemId = addResponse.getBody().items().get(0).id();

        ResponseEntity<CartResponse> getResponse = restTemplate.exchange(
                "/api/cart", HttpMethod.GET, new HttpEntity<>(headers), CartResponse.class);
        assertThat(getResponse.getBody().items()).hasSize(1);
        assertThat(getResponse.getBody().totalAmount())
                .isEqualByComparingTo(variant.getPrice().multiply(BigDecimal.valueOf(2)));

        ResponseEntity<CartResponse> updateResponse = restTemplate.exchange(
                "/api/cart/items/" + itemId, HttpMethod.PUT,
                new HttpEntity<>(new UpdateCartItemRequest(5), headers),
                CartResponse.class);
        assertThat(updateResponse.getBody().items().get(0).quantity()).isEqualTo(5);

        ResponseEntity<CartResponse> removeResponse = restTemplate.exchange(
                "/api/cart/items/" + itemId, HttpMethod.DELETE,
                new HttpEntity<>(headers), CartResponse.class);
        assertThat(removeResponse.getBody().items()).isEmpty();
    }

    @Test
    void addingMoreThanStockReturns400() {
        String token = registerAndGetToken("cart-user2");
        ProductVariant variant = productVariantRepository.findAll().get(0);
        HttpHeaders headers = authHeaders(token);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/cart/items", HttpMethod.POST,
                new HttpEntity<>(new AddCartItemRequest(variant.getId(), variant.getStockQuantity() + 1), headers),
                String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void usersCannotSeeEachOthersCart() {
        String tokenA = registerAndGetToken("cart-userA");
        String tokenB = registerAndGetToken("cart-userB");
        ProductVariant variant = productVariantRepository.findAll().get(0);

        restTemplate.exchange("/api/cart/items", HttpMethod.POST,
                new HttpEntity<>(new AddCartItemRequest(variant.getId(), 1), authHeaders(tokenA)),
                CartResponse.class);

        ResponseEntity<CartResponse> cartB = restTemplate.exchange(
                "/api/cart", HttpMethod.GET, new HttpEntity<>(authHeaders(tokenB)), CartResponse.class);

        assertThat(cartB.getBody().items()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private String registerAndGetToken(String emailPrefix) {
        Map<String, String> body = Map.of(
                "email", emailPrefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "password", "password123",
                "fullName", "Cart Tester");
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
