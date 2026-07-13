package com.splashcan.backend.cart;

import com.splashcan.backend.cart.dto.AddCartItemRequest;
import com.splashcan.backend.cart.dto.CartResponse;
import com.splashcan.backend.cart.dto.UpdateCartItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Authenticated user's shopping cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get the current user's cart")
    @ApiResponse(responseCode = "200", description = "Cart returned")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        return cartService.getCart(authentication.getName());
    }

    @Operation(summary = "Add a product variant to the cart")
    @ApiResponse(responseCode = "200", description = "Item added, updated cart returned")
    @ApiResponse(responseCode = "400", description = "Validation failed or insufficient stock")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "404", description = "Variant not found")
    @PostMapping("/items")
    public CartResponse addItem(Authentication authentication, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(authentication.getName(), request);
    }

    @Operation(summary = "Update the quantity of a cart item")
    @ApiResponse(responseCode = "200", description = "Item updated, updated cart returned")
    @ApiResponse(responseCode = "400", description = "Validation failed or insufficient stock")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    @PutMapping("/items/{id}")
    public CartResponse updateItem(Authentication authentication, @PathVariable Long id,
                                    @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(authentication.getName(), id, request);
    }

    @Operation(summary = "Remove an item from the cart")
    @ApiResponse(responseCode = "200", description = "Item removed, updated cart returned")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    @DeleteMapping("/items/{id}")
    public CartResponse removeItem(Authentication authentication, @PathVariable Long id) {
        return cartService.removeItem(authentication.getName(), id);
    }
}
