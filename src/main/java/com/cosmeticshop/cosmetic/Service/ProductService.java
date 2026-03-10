package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateProductRequest;
import com.cosmeticshop.cosmetic.Dto.InventorySummaryResponse;
import com.cosmeticshop.cosmetic.Dto.ProductResponse;
import com.cosmeticshop.cosmetic.Dto.UpdateProductRequest;
import com.cosmeticshop.cosmetic.Entity.Brand;
import com.cosmeticshop.cosmetic.Entity.Category;
import com.cosmeticshop.cosmetic.Entity.Product;
import com.cosmeticshop.cosmetic.Exception.ResourceNotFoundException;
import com.cosmeticshop.cosmetic.Repository.BrandRepository;
import com.cosmeticshop.cosmetic.Repository.CategoryRepository;
import com.cosmeticshop.cosmetic.Repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final IAuditLogService auditLogService;

    public ProductService(ProductRepository productRepository,
                         BrandRepository brandRepository,
                         CategoryRepository categoryRepository,
                         IAuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.auditLogService = auditLogService;
    }

        // Trả toàn bộ sản phẩm theo dạng DTO để controller trả JSON ổn định.
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();

        // Admin/employee xem toàn bộ sản phẩm (kể cả hidden); khách chỉ xem sản phẩm visible.
        if (!isPrivilegedInventoryRole()) {
            products = products.stream()
                    .filter(this::isVisibleForCustomer)
                    .collect(Collectors.toList());
        }

        return products.stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

        // Lấy chi tiết một sản phẩm, ném lỗi 404 nếu id không tồn tại.
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với id: " + id));

        if (!isPrivilegedInventoryRole() && !isVisibleForCustomer(product)) {
            throw new ResourceNotFoundException("Không tìm thấy Product với id: " + id);
        }

        return toProductResponse(product);
    }

    public List<ProductResponse> getEmployeeInventory(String query, Long brandId, Long categoryId, String visibility) {
        String normalizedQuery = normalize(query);
        String normalizedVisibility = normalize(visibility);

        return productRepository.findAll().stream()
                .filter(product -> filterByProductText(product, normalizedQuery))
                .filter(product -> brandId == null || (product.getBrand() != null && brandId.equals(product.getBrand().getId())))
                .filter(product -> categoryId == null || (product.getCategory() != null && categoryId.equals(product.getCategory().getId())))
                .filter(product -> filterByVisibility(product, normalizedVisibility))
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }
    
        // Tạo sản phẩm mới: kiểm tra brand/category hợp lệ trước khi gán dữ liệu sản phẩm.
    public ProductResponse createProduct(CreateProductRequest request) {
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Brand với id: " + request.getBrandId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Category với id: " + request.getCategoryId()));

        Product product = new Product();
        product.setBrand(brand);
        product.setCategory(category);
        applyProductFields(
                product,
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                request.getImageUrl(),
            request.getStockQuantity(),
            request.getExpiryDate(),
            request.getVisible());

        Product savedProduct = productRepository.save(product);
        return toProductResponse(savedProduct);
    }

        // Cập nhật sản phẩm hiện có theo id.
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với id: " + id));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Brand với id: " + request.getBrandId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Category với id: " + request.getCategoryId()));

        product.setBrand(brand);
        product.setCategory(category);
        applyProductFields(
                product,
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                request.getImageUrl(),
                request.getStockQuantity(),
                request.getExpiryDate(),
                request.getVisible());

        return toProductResponse(productRepository.save(product));
    }

    public ProductResponse updateStockByEmployee(Long productId, Integer stockQuantity, String reason) {
        if (stockQuantity == null || stockQuantity < 0) {
            throw new RuntimeException("Tồn kho không hợp lệ");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với id: " + productId));

        Integer oldStock = product.getStockQuantity();
        product.setStockQuantity(stockQuantity);

        Product saved = productRepository.save(product);
        auditLogService.logAction(
                "EMPLOYEE_UPDATE_STOCK",
                "product#" + productId,
                String.format("Cập nhật tồn kho: %s -> %s | reason=%s",
                        oldStock == null ? "N/A" : oldStock,
                        stockQuantity,
                        safeNote(reason)));

        return toProductResponse(saved);
    }

    public ProductResponse updateVisibilityByEmployee(Long productId, Boolean visible, String reason) {
        if (visible == null) {
            throw new RuntimeException("visible không được để trống");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với id: " + productId));

        Boolean oldVisible = product.getVisible();
        product.setVisible(visible);

        Product saved = productRepository.save(product);
        auditLogService.logAction(
                "EMPLOYEE_UPDATE_PRODUCT_VISIBILITY",
                "product#" + productId,
                String.format("Ẩn/hiện sản phẩm: %s -> %s | reason=%s",
                        oldVisible,
                        visible,
                        safeNote(reason)));

        return toProductResponse(saved);
    }

    public ProductResponse updateExpiryByEmployee(Long productId, LocalDate expiryDate, String note) {
        if (expiryDate == null) {
            throw new RuntimeException("expiryDate không được để trống");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với id: " + productId));

        LocalDate oldExpiryDate = product.getExpiryDate();
        product.setExpiryDate(expiryDate);

        Product saved = productRepository.save(product);
        auditLogService.logAction(
                "EMPLOYEE_UPDATE_PRODUCT_EXPIRY",
                "product#" + productId,
                String.format("Cập nhật hạn dùng: %s -> %s | note=%s",
                        oldExpiryDate,
                        expiryDate,
                        safeNote(note)));

        return toProductResponse(saved);
    }

        // Xóa sản phẩm sau khi xác nhận tồn tại.
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với id: " + id));
        productRepository.delete(product);
    }

        // Tổng hợp KPI tồn kho cấp vĩ mô để phục vụ quyết định nhập hàng.
    public InventorySummaryResponse getInventorySummary(int lowStockThreshold) {
        int threshold = Math.max(0, lowStockThreshold);
        List<Product> products = productRepository.findAll();

        long totalProducts = products.size();
        long totalStockUnits = products.stream()
                .mapToLong(item -> safeStock(item.getStockQuantity()))
                .sum();
        double totalInventoryValue = products.stream()
                .mapToDouble(item -> safeStock(item.getStockQuantity()) * safePrice(item.getPrice()))
                .sum();

        long outOfStockProducts = products.stream()
                .filter(item -> safeStock(item.getStockQuantity()) == 0)
                .count();

        // Gợi ý nhập thêm cho các sản phẩm dưới ngưỡng cảnh báo.
        List<InventorySummaryResponse.RestockSuggestion> suggestions = products.stream()
                .filter(item -> safeStock(item.getStockQuantity()) <= threshold)
                .sorted(Comparator.comparingInt((Product item) -> safeStock(item.getStockQuantity())))
                .map(item -> {
                    int currentStock = safeStock(item.getStockQuantity());
                    int recommendedRestockUnits = Math.max(0, threshold * 2 - currentStock);
                    double estimatedBudget = recommendedRestockUnits * safePrice(item.getPrice());
                    return new InventorySummaryResponse.RestockSuggestion(
                            item.getId(),
                            item.getName(),
                            currentStock,
                            recommendedRestockUnits,
                            estimatedBudget);
                })
                .collect(Collectors.toList());

        return new InventorySummaryResponse(
                totalProducts,
                totalStockUnits,
                totalInventoryValue,
                suggestions.size(),
                outOfStockProducts,
                threshold,
                suggestions);
    }

        // TODO tạm thời: hiện trả toàn bộ danh sách, chưa có full-text search theo query.
    public List<ProductResponse> searchProduct(String query) {
        String normalizedQuery = normalize(query);
        return getAllProducts().stream()
            .filter(item -> filterByProductText(item, normalizedQuery))
            .collect(Collectors.toList());
    }

        // Gom tất cả rule validate + normalize field để tái sử dụng cho cả create/update.
    private void applyProductFields(
            Product product,
            String name,
            Double price,
            String description,
            String imageUrl,
            Integer stockQuantity,
            LocalDate expiryDate,
            Boolean visible) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Tên sản phẩm không được để trống");
        }
        if (price == null || price <= 0) {
            throw new RuntimeException("Giá sản phẩm phải lớn hơn 0");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new RuntimeException("Tồn kho không hợp lệ");
        }

        product.setName(name.trim());
        product.setPrice(price);
        product.setDescription(description == null ? "" : description.trim());
                // Nếu thiếu ảnh, dùng placeholder để tránh null/blank ở UI.
                product.setImageUrl(
                                imageUrl == null || imageUrl.trim().isEmpty()
                                                ? "https://via.placeholder.com/300"
                                                : imageUrl.trim());
        product.setStockQuantity(stockQuantity);
        product.setExpiryDate(expiryDate);
        product.setVisible(visible == null ? Boolean.TRUE : visible);
    }

        // Chuẩn hóa tồn kho null/âm về giá trị an toàn để tính toán thống kê.
    private int safeStock(Integer stockQuantity) {
        return stockQuantity == null ? 0 : Math.max(stockQuantity, 0);
    }

        // Chuẩn hóa giá null/âm về giá trị an toàn để tính tổng giá trị kho.
    private double safePrice(Double price) {
        return price == null ? 0 : Math.max(price, 0);
    }

        // Mapping entity sang response DTO để tách biệt model DB và dữ liệu trả ra API.
    private ProductResponse toProductResponse(Product product) {
        ProductResponse.BrandSummary brandSummary = null;
        if (product.getBrand() != null) {
            brandSummary = new ProductResponse.BrandSummary(
                    product.getBrand().getId(),
                    product.getBrand().getName(),
                    product.getBrand().getOrigin());
        }

        ProductResponse.CategorySummary categorySummary = null;
        if (product.getCategory() != null) {
            categorySummary = new ProductResponse.CategorySummary(
                    product.getCategory().getId(),
                    product.getCategory().getName());
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getImageUrl(),
                product.getStockQuantity(),
                product.getExpiryDate(),
                product.getVisible(),
                brandSummary,
                categorySummary);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String safeNote(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value.trim();
    }

    private boolean filterByProductText(Product product, String normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            return true;
        }

        String name = product.getName() == null ? "" : product.getName().toLowerCase(Locale.ROOT);
        String description = product.getDescription() == null ? "" : product.getDescription().toLowerCase(Locale.ROOT);
        String brandName = product.getBrand() == null || product.getBrand().getName() == null
                ? ""
                : product.getBrand().getName().toLowerCase(Locale.ROOT);
        String categoryName = product.getCategory() == null || product.getCategory().getName() == null
                ? ""
                : product.getCategory().getName().toLowerCase(Locale.ROOT);

        return String.valueOf(product.getId()).contains(normalizedQuery)
                || name.contains(normalizedQuery)
                || description.contains(normalizedQuery)
                || brandName.contains(normalizedQuery)
                || categoryName.contains(normalizedQuery);
    }

    private boolean filterByProductText(ProductResponse product, String normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            return true;
        }

        String name = product.getName() == null ? "" : product.getName().toLowerCase(Locale.ROOT);
        String description = product.getDescription() == null ? "" : product.getDescription().toLowerCase(Locale.ROOT);
        String brandName = product.getBrand() == null || product.getBrand().getName() == null
                ? ""
                : product.getBrand().getName().toLowerCase(Locale.ROOT);
        String categoryName = product.getCategory() == null || product.getCategory().getName() == null
                ? ""
                : product.getCategory().getName().toLowerCase(Locale.ROOT);

        return String.valueOf(product.getId()).contains(normalizedQuery)
                || name.contains(normalizedQuery)
                || description.contains(normalizedQuery)
                || brandName.contains(normalizedQuery)
                || categoryName.contains(normalizedQuery);
    }

    private boolean filterByVisibility(Product product, String normalizedVisibility) {
        if (normalizedVisibility.isEmpty() || normalizedVisibility.equals("all")) {
            return true;
        }

        boolean visible = !Boolean.FALSE.equals(product.getVisible());
        if (normalizedVisibility.equals("visible")) {
            return visible;
        }
        if (normalizedVisibility.equals("hidden")) {
            return !visible;
        }
        return true;
    }

    private boolean isVisibleForCustomer(Product product) {
        return !Boolean.FALSE.equals(product.getVisible());
    }

    private boolean isPrivilegedInventoryRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String name = authority.getAuthority();
            if ("ROLE_ADMIN".equals(name) || "ROLE_EMPLOYEE".equals(name)) {
                return true;
            }
        }

        return false;
    }
}
