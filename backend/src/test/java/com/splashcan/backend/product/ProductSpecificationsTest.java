package com.splashcan.backend.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static com.splashcan.backend.product.ProductSpecifications.hasCategorySlug;
import static com.splashcan.backend.product.ProductSpecifications.hasFlavor;
import static com.splashcan.backend.product.ProductSpecifications.isActive;
import static com.splashcan.backend.product.ProductSpecifications.priceBetween;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductSpecificationsTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void filtersByCategoryAndFlavor() {
        Specification<Product> spec = Specification.where(isActive())
                .and(hasCategorySlug("soda"))
                .and(hasFlavor("Cam chanh"));

        List<Product> results = productRepository.findAll(spec);

        assertThat(results).extracting(Product::getName).containsExactly("SplashCan Citrus Blast");
    }

    @Test
    void filtersByPriceRange() {
        Specification<Product> spec = Specification.where(isActive())
                .and(priceBetween(new BigDecimal("18000"), new BigDecimal("25000")));

        List<Product> results = productRepository.findAll(spec);

        assertThat(results).extracting(Product::getName).containsExactly("SplashCan Energy Rush");
    }

    @Test
    void noFiltersReturnsAllActiveProducts() {
        Specification<Product> spec = Specification.where(isActive());

        List<Product> results = productRepository.findAll(spec);

        assertThat(results).hasSizeGreaterThanOrEqualTo(3);
    }
}
