package com.cosmeticshop.cosmetic.Service;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateProductRequest;
import com.cosmeticshop.cosmetic.Dto.ProductResponse;
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

    //Lấy tất cả sản phẩm
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    //lấy sản phẩm theo id
    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với id: " + id));
        return toProductResponse(product);
    }
    
    //lưu sản phẩm
    public ProductResponse createProduct(CreateProductRequest request){

        Brand brand = new Brand();
        brand.setName(request.getBrandName());
        brand.setOrigin(request.getOrigin());
        Category category = new Category();
        category.setName(request.getCategoryName());    

        // Tìm Brand từ database
        brand = brandRepository.findById(request.getBrandId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Brand với id: " + request.getBrandId()));
        
        // Tìm Category từ database
        category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Category với id: " + request.getCategoryId()));

        // Tạo Product entity
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setStockQuantity(request.getStockQuantity());
        product.setBrand(brand);
        product.setCategory(category);
        
        Product savedProduct = productRepository.save(product);
        return toProductResponse(savedProduct);
    }

    public List<ProductResponse> searchProduct(String query){
        // Implement search logic here
        return productRepository.findAll().stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

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
