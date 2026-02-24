package com.cosmeticshop.cosmetic.Service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateProductRequest;
import com.cosmeticshop.cosmetic.Entity.Brand;
import com.cosmeticshop.cosmetic.Entity.Category;
import com.cosmeticshop.cosmetic.Entity.Product;
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
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    //lấy sản phẩm theo id
    public Product getProductById(Long id){
        return productRepository.findById(id).orElse(null);
    }
    
    //lưu sản phẩm
    public Product createProduct(CreateProductRequest request){

        Brand brand = new Brand();
        brand.setName(request.getBrandName());
        brand.setOrigin(request.getOrigin());
        Category category = new Category();
        category.setName(request.getCategoryName());    

        // Tìm Brand từ database
        brand = brandRepository.findById(request.getBrandId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Brand với id: " + request.getBrandId()));
        
        // Tìm Category từ database
        category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Category với id: " + request.getCategoryId()));

        // Tạo Product entity
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setStockQuantity(request.getStockQuantity());
        product.setBrand(brand);
        product.setCategory(category);
        
        brandRepository.save(brand);
        categoryRepository.save(category);
        return productRepository.save(product);
    }

    public List<Product> searchProduct(String query){
        // Implement search logic here
        return productRepository.findAll(); // Placeholder
    }
}
