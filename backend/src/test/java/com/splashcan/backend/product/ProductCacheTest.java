package com.splashcan.backend.product;

import com.splashcan.backend.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductCacheTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findByIdIsCachedAndSurvivesSerializationRoundTrip() {
        Long id = productRepository.findAll().get(0).getId();
        cacheManager.getCache("products").clear();

        ProductResponse first = productService.findById(id);

        ProductResponse cached = cacheManager.getCache("products").get(id, ProductResponse.class);
        assertThat(cached).isNotNull();
        assertThat(cached.id()).isEqualTo(id);

        ProductResponse second = productService.findById(id);
        assertThat(second).isEqualTo(first);
    }
}
