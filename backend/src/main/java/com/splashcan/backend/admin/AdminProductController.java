package com.splashcan.backend.admin;

import com.splashcan.backend.admin.dto.AdminProductRequest;
import com.splashcan.backend.admin.dto.AdminVariantRequest;
import com.splashcan.backend.product.dto.ProductResponse;
import com.splashcan.backend.product.dto.ProductVariantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin - Products", description = "Admin-only product and variant management")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @Operation(summary = "Create a new product")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminProductService.createProduct(request));
    }

    @Operation(summary = "Update an existing product")
    @ApiResponse(responseCode = "200", description = "Product updated")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin")
    @ApiResponse(responseCode = "404", description = "Product or category not found")
    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody AdminProductRequest request) {
        return adminProductService.updateProduct(id, request);
    }

    @Operation(summary = "Delete a product")
    @ApiResponse(responseCode = "204", description = "Product deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add a variant to a product")
    @ApiResponse(responseCode = "201", description = "Variant created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PostMapping("/products/{productId}/variants")
    public ResponseEntity<ProductVariantResponse> addVariant(@PathVariable Long productId,
                                                              @Valid @RequestBody AdminVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminProductService.addVariant(productId, request));
    }

    @Operation(summary = "Update a product variant")
    @ApiResponse(responseCode = "200", description = "Variant updated")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin")
    @ApiResponse(responseCode = "404", description = "Variant not found")
    @ApiResponse(responseCode = "409", description = "Variant was concurrently modified (optimistic locking conflict)")
    @PutMapping("/variants/{id}")
    public ProductVariantResponse updateVariant(@PathVariable Long id, @Valid @RequestBody AdminVariantRequest request) {
        return adminProductService.updateVariant(id, request);
    }

    @Operation(summary = "Delete a product variant")
    @ApiResponse(responseCode = "204", description = "Variant deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin")
    @ApiResponse(responseCode = "404", description = "Variant not found")
    @DeleteMapping("/variants/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        adminProductService.deleteVariant(id);
        return ResponseEntity.noContent().build();
    }
}
