package com.splashcan.backend.category;

import com.splashcan.backend.category.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Public category listing")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "List all categories")
    @ApiResponse(responseCode = "200", description = "Categories returned")
    @GetMapping
    public List<CategoryResponse> getCategories() {
        return categoryService.findAll();
    }
}
