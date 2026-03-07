package com.cosmeticshop.cosmetic.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public ProductService(ProductRepository productRepository,
                         BrandRepository brandRepository,
                         CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

        // Trả toàn bộ sản phẩm theo dạng DTO để controller trả JSON ổn định.
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

        // Lấy chi tiết một sản phẩm, ném lỗi 404 nếu id không tồn tại.
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với id: " + id));
        return toProductResponse(product);
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
                request.getStockQuantity());

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
                request.getStockQuantity());

        return toProductResponse(productRepository.save(product));
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
        return productRepository.findAll().stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

        // Gom tất cả rule validate + normalize field để tái sử dụng cho cả create/update.
    private void applyProductFields(
            Product product,
            String name,
            Double price,
            String description,
            String imageUrl,
            Integer stockQuantity) {
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
                brandSummary,
                categorySummary);
    }
}
