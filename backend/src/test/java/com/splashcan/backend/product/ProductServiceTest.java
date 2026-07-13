package com.splashcan.backend.product;

import com.splashcan.backend.category.Category;
import com.splashcan.backend.exception.ProductNotFoundException;
import com.splashcan.backend.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct(boolean active) {
        Category category = Category.builder().id(1L).name("Juice").slug("juice").build();
        return Product.builder()
                .id(1L)
                .name("Splash Can")
                .description("A refreshing can")
                .basePrice(BigDecimal.TEN)
                .category(category)
                .active(active)
                .build();
    }

    /** Stubs {@code findAll} to capture the {@link Pageable} it's invoked with, then runs findProducts. */
    private Pageable capturePageable(int page, int size, String sort) {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(productRepository.findAll(any(Specification.class), captor.capture()))
                .thenReturn(new PageImpl<>(List.of()));

        productService.findProducts(null, null, null, null, page, size, sort);

        return captor.getValue();
    }

    @Test
    void findProductsMapsRepositoryPageToProductResponses() {
        Product product = sampleProduct(true);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = productService.findProducts(
                "juice", "mango", BigDecimal.ONE, BigDecimal.TEN, 0, 20, null);

        assertThat(result.getContent()).hasSize(1);
        ProductResponse response = result.getContent().get(0);
        assertThat(response.id()).isEqualTo(product.getId());
        assertThat(response.name()).isEqualTo(product.getName());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findProductsClampsNonPositiveSizeToOne() {
        assertThat(capturePageable(0, 0, null).getPageSize()).isEqualTo(1);
        assertThat(capturePageable(0, -10, null).getPageSize()).isEqualTo(1);
    }

    @Test
    void findProductsClampsOversizedPageSizeToMax() {
        assertThat(capturePageable(0, 500, null).getPageSize()).isEqualTo(100);
    }

    @Test
    void findProductsKeepsNormalPageSizeUnchanged() {
        assertThat(capturePageable(0, 20, null).getPageSize()).isEqualTo(20);
    }

    @Test
    void findProductsClampsNegativePageNumberToZero() {
        assertThat(capturePageable(-5, 20, null).getPageNumber()).isEqualTo(0);
    }

    @Test
    void findProductsSortsByPriceDescendingWhenRequested() {
        Sort.Order order = capturePageable(0, 20, "price,desc").getSort().getOrderFor("basePrice");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void findProductsSortsByNameAscendingByDefaultDirection() {
        Sort.Order order = capturePageable(0, 20, "name").getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void findProductsDefaultsToCreatedAtDescendingWhenSortMissing() {
        assertThat(capturePageable(0, 20, null).getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
        assertThat(capturePageable(0, 20, "  ").getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void findProductsDefaultsToCreatedAtForUnrecognizedSortProperty() {
        Sort.Order order = capturePageable(0, 20, "bogus").getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
    }

    @Test
    void findByIdReturnsResponseWhenActive() {
        Product product = sampleProduct(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Splash Can");
    }

    @Test
    void findByIdThrowsWhenProductIsInactive() {
        Product product = sampleProduct(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.findById(1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void findByIdThrowsWhenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
