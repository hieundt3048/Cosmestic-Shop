package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.EmployeeReviewResponse;
import com.cosmeticshop.cosmetic.Dto.PublicReviewResponse;
import com.cosmeticshop.cosmetic.Entity.Product;
import com.cosmeticshop.cosmetic.Entity.Review;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Exception.ResourceNotFoundException;
import com.cosmeticshop.cosmetic.Repository.ProductRepository;
import com.cosmeticshop.cosmetic.Repository.ReviewRepository;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final IAuditLogService auditLogService;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            IAuditLogService auditLogService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    public List<EmployeeReviewResponse> getEmployeeReviews(String status, String query) {
        String normalizedStatus = normalize(status);
        String normalizedQuery = normalize(query);

        return reviewRepository.findAll().stream()
                .filter(review -> filterByStatus(review, normalizedStatus))
                .filter(review -> filterByQuery(review, normalizedQuery))
                .sorted((left, right) -> safeDate(right.getCreatedAt()).compareTo(safeDate(left.getCreatedAt())))
                .map(this::toEmployeeReviewResponse)
                .collect(Collectors.toList());
    }

    public EmployeeReviewResponse moderateReview(Long reviewId, String action, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay review voi id: " + reviewId));

        Review.ModerationStatus nextStatus = parseAction(action);
        Review.ModerationStatus oldStatus = resolveStatus(review);

        review.setModerationStatus(nextStatus);
        review.setModerationReason(cleanReason(reason));
        review.setModeratedAt(LocalDateTime.now());
        review.setModeratedBy(getCurrentUsername());

        Review saved = reviewRepository.save(review);
        auditLogService.logAction(
                "EMPLOYEE_MODERATE_REVIEW",
                "review#" + reviewId,
                String.format("Moderation: %s -> %s | reason=%s",
                        oldStatus.name(),
                        nextStatus.name(),
                        saved.getModerationReason() == null ? "N/A" : saved.getModerationReason()));

        return toEmployeeReviewResponse(saved);
    }

    public List<PublicReviewResponse> getPublicReviewsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay Product voi id: " + productId));

        if (Boolean.FALSE.equals(product.getVisible())) {
            throw new ResourceNotFoundException("Khong tim thay Product voi id: " + productId);
        }

        return reviewRepository.findByProductIdOrderByCreatedAtDesc(product.getId()).stream()
                .filter(review -> resolveStatus(review) == Review.ModerationStatus.APPROVED)
                .map(this::toPublicReviewResponse)
                .collect(Collectors.toList());
    }

    private EmployeeReviewResponse toEmployeeReviewResponse(Review review) {
        Optional<User> customer = review.getUserId() == null
                ? Optional.empty()
                : userRepository.findById(review.getUserId());

        User user = customer.orElse(null);
        String customerName = "Khach #" + (review.getUserId() == null ? "N/A" : review.getUserId());
        if (user != null) {
            String fullName = safeTrim(user.getFullName());
            customerName = fullName.isEmpty() ? safeDefault(user.getUsername(), customerName) : fullName;
        }

        String productName = review.getProduct() == null ? "N/A" : safeDefault(review.getProduct().getName(), "N/A");
        Long productId = review.getProduct() == null ? null : review.getProduct().getId();
        Review.ModerationStatus status = resolveStatus(review);

        return new EmployeeReviewResponse(
                review.getId(),
                productId,
                productName,
                review.getUserId(),
                customerName,
                user == null ? null : user.getEmail(),
                user == null ? null : user.getPhone(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                status.name(),
                review.getModerationReason(),
                review.getModeratedAt(),
                review.getModeratedBy());
    }

    private PublicReviewResponse toPublicReviewResponse(Review review) {
        String customerName = "Khach hang";
        if (review.getUserId() != null) {
            Optional<User> user = userRepository.findById(review.getUserId());
            if (user.isPresent()) {
                String fullName = safeTrim(user.get().getFullName());
                customerName = fullName.isEmpty() ? safeDefault(user.get().getUsername(), customerName) : fullName;
            }
        }

        return new PublicReviewResponse(
                review.getId(),
                customerName,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt());
    }

    private Review.ModerationStatus parseAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            throw new RuntimeException("action khong duoc de trong");
        }

        String normalized = action.trim().toUpperCase(Locale.ROOT);
        if ("APPROVE".equals(normalized)) {
            return Review.ModerationStatus.APPROVED;
        }
        if ("HIDE".equals(normalized)) {
            return Review.ModerationStatus.HIDDEN;
        }
        throw new RuntimeException("action khong hop le. Chi chap nhan APPROVE/HIDE");
    }

    private Review.ModerationStatus resolveStatus(Review review) {
        return review.getModerationStatus() == null ? Review.ModerationStatus.APPROVED : review.getModerationStatus();
    }

    private boolean filterByStatus(Review review, String normalizedStatus) {
        if (normalizedStatus.isEmpty() || "all".equals(normalizedStatus)) {
            return true;
        }
        return resolveStatus(review).name().equalsIgnoreCase(normalizedStatus);
    }

    private boolean filterByQuery(Review review, String normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            return true;
        }

        String productName = review.getProduct() == null ? "" : safeDefault(review.getProduct().getName(), "").toLowerCase(Locale.ROOT);
        String comment = safeDefault(review.getComment(), "").toLowerCase(Locale.ROOT);

        Optional<User> customer = review.getUserId() == null
                ? Optional.empty()
                : userRepository.findById(review.getUserId());

        String customerName = customer
                .map(item -> {
                    String fullName = safeTrim(item.getFullName());
                    return fullName.isEmpty() ? safeDefault(item.getUsername(), "") : fullName;
                })
                .orElse("")
                .toLowerCase(Locale.ROOT);

        String customerEmail = customer.map(item -> safeDefault(item.getEmail(), "")).orElse("").toLowerCase(Locale.ROOT);
        String customerPhone = customer.map(item -> safeDefault(item.getPhone(), "")).orElse("").toLowerCase(Locale.ROOT);

        return String.valueOf(review.getId()).contains(normalizedQuery)
                || productName.contains(normalizedQuery)
                || comment.contains(normalizedQuery)
                || customerName.contains(normalizedQuery)
                || customerEmail.contains(normalizedQuery)
                || customerPhone.contains(normalizedQuery);
    }

    private String normalize(String input) {
        return input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
    }

    private String cleanReason(String reason) {
        if (reason == null) {
            return null;
        }

        String normalized = reason.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private LocalDateTime safeDate(LocalDateTime value) {
        return value == null ? LocalDateTime.MIN : value;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }

        String principalName = authentication.getName();
        return principalName == null || principalName.isBlank() ? "system" : principalName;
    }
}
