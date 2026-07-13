package com.splashcan.backend.admin;

import com.splashcan.backend.admin.dto.AdminProductRequest;
import com.splashcan.backend.admin.dto.AdminVariantRequest;
import com.splashcan.backend.product.dto.ProductResponse;
import com.splashcan.backend.product.dto.ProductVariantResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminProductService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody AdminProductRequest request) {
        return adminProductService.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/products/{productId}/variants")
    public ResponseEntity<ProductVariantResponse> addVariant(@PathVariable Long productId,
                                                              @Valid @RequestBody AdminVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminProductService.addVariant(productId, request));
    }

    @PutMapping("/variants/{id}")
    public ProductVariantResponse updateVariant(@PathVariable Long id, @Valid @RequestBody AdminVariantRequest request) {
        return adminProductService.updateVariant(id, request);
    }

    @DeleteMapping("/variants/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        adminProductService.deleteVariant(id);
        return ResponseEntity.noContent().build();
    }
}
