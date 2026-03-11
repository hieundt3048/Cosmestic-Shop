package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.EmployeeReviewResponse;
import com.cosmeticshop.cosmetic.Dto.ModerateReviewRequest;
import com.cosmeticshop.cosmetic.Service.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products/employee/reviews")
@PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
public class EmployeeReviewController {

    private final ReviewService reviewService;

    public EmployeeReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeReviewResponse>> getEmployeeReviews(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(reviewService.getEmployeeReviews(status, q));
    }

    @PatchMapping("/{reviewId}/moderation")
    public ResponseEntity<EmployeeReviewResponse> moderateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ModerateReviewRequest request) {
        return ResponseEntity.ok(reviewService.moderateReview(reviewId, request.getAction(), request.getReason()));
    }
}
