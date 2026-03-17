package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.WishlistItemResponse;
import com.cosmeticshop.cosmetic.Service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
@PreAuthorize("hasRole('CUSTOMER')")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<WishlistItemResponse>> getMyWishlist(Authentication authentication) {
        String username = authentication == null ? "" : authentication.getName();
        return ResponseEntity.ok(wishlistService.getMyWishlist(username));
    }

    @PostMapping("/my/{productId}")
    public ResponseEntity<WishlistItemResponse> addToWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        String username = authentication == null ? "" : authentication.getName();
        return ResponseEntity.status(201).body(wishlistService.addToWishlist(username, productId));
    }

    @DeleteMapping("/my/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        String username = authentication == null ? "" : authentication.getName();
        wishlistService.removeFromWishlist(username, productId);
        return ResponseEntity.noContent().build();
    }
}
