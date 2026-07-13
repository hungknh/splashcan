package com.splashcan.backend.product;

import com.splashcan.backend.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public product catalog browsing")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "List products with optional category/flavor/price filtering, sorting, and pagination")
    @ApiResponse(responseCode = "200", description = "Page of products returned")
    @GetMapping
    public Page<ProductResponse> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String flavor,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return productService.findProducts(category, flavor, minPrice, maxPrice, page, size, sort);
    }

    @Operation(summary = "Get a single product by id")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.findById(id);
    }
}
