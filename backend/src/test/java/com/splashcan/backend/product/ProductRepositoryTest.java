package com.splashcan.backend.product;

import com.splashcan.backend.category.Category;
import com.splashcan.backend.category.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Test
    void savesProductWithVariantAndReloadsRelationship() {
        Category category = categoryRepository.save(Category.builder()
                .name("Soda Test")
                .slug("soda-test")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Test Can")
                .description("desc")
                .basePrice(new BigDecimal("15000.00"))
                .category(category)
                .active(true)
                .build());

        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .flavor("Original")
                .sizeMl(330)
                .price(new BigDecimal("15000.00"))
                .stockQuantity(100)
                .sku("TEST-330")
                .build());

        ProductVariant reloaded = productVariantRepository.findById(variant.getId()).orElseThrow();

        assertThat(reloaded.getProduct().getId()).isEqualTo(product.getId());
        assertThat(reloaded.getProduct().getCategory().getSlug()).isEqualTo(category.getSlug());
    }
}
