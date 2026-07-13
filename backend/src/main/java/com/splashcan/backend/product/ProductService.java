package com.splashcan.backend.product;

import com.splashcan.backend.exception.ProductNotFoundException;
import com.splashcan.backend.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.splashcan.backend.product.ProductSpecifications.hasCategorySlug;
import static com.splashcan.backend.product.ProductSpecifications.hasFlavor;
import static com.splashcan.backend.product.ProductSpecifications.isActive;
import static com.splashcan.backend.product.ProductSpecifications.priceBetween;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> findProducts(String category, String flavor, BigDecimal minPrice, BigDecimal maxPrice,
                                               int page, int size, String sort) {
        Specification<Product> spec = Specification.where(isActive())
                .and(hasCategorySlug(category))
                .and(hasFlavor(flavor))
                .and(priceBetween(minPrice, maxPrice));

        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), resolveSort(sort));
        return productRepository.findAll(spec, pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .filter(Product::isActive)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private int clampSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = switch (parts[0]) {
            case "price" -> "basePrice";
            case "name" -> "name";
            default -> "createdAt";
        };
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
}
