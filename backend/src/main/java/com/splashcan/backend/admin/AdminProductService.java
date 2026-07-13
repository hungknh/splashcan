package com.splashcan.backend.admin;

import com.splashcan.backend.admin.dto.AdminProductRequest;
import com.splashcan.backend.admin.dto.AdminVariantRequest;
import com.splashcan.backend.category.Category;
import com.splashcan.backend.category.CategoryRepository;
import com.splashcan.backend.exception.CategoryNotFoundException;
import com.splashcan.backend.exception.ProductNotFoundException;
import com.splashcan.backend.exception.VariantNotFoundException;
import com.splashcan.backend.product.Product;
import com.splashcan.backend.product.ProductRepository;
import com.splashcan.backend.product.ProductVariant;
import com.splashcan.backend.product.ProductVariantRepository;
import com.splashcan.backend.product.dto.ProductResponse;
import com.splashcan.backend.product.dto.ProductVariantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse createProduct(AdminProductRequest request) {
        Category category = findCategory(request.categoryId());
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .basePrice(request.basePrice())
                .category(category)
                .thumbnailUrl(request.thumbnailUrl())
                .model3dUrl(request.model3dUrl())
                .active(request.active())
                .build();
        productRepository.save(product);
        return ProductResponse.from(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponse updateProduct(Long id, AdminProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        Category category = findCategory(request.categoryId());

        product.setName(request.name());
        product.setDescription(request.description());
        product.setBasePrice(request.basePrice());
        product.setCategory(category);
        product.setThumbnailUrl(request.thumbnailUrl());
        product.setModel3dUrl(request.model3dUrl());
        product.setActive(request.active());
        productRepository.save(product);
        return ProductResponse.from(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductVariantResponse addVariant(Long productId, AdminVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .flavor(request.flavor())
                .sizeMl(request.sizeMl())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .sku(request.sku())
                .build();
        productVariantRepository.save(variant);
        return ProductVariantResponse.from(variant);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductVariantResponse updateVariant(Long variantId, AdminVariantRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException(variantId));
        variant.setFlavor(request.flavor());
        variant.setSizeMl(request.sizeMl());
        variant.setPrice(request.price());
        variant.setStockQuantity(request.stockQuantity());
        variant.setSku(request.sku());
        productVariantRepository.save(variant);
        return ProductVariantResponse.from(variant);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void deleteVariant(Long variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new VariantNotFoundException(variantId);
        }
        productVariantRepository.deleteById(variantId);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }
}
