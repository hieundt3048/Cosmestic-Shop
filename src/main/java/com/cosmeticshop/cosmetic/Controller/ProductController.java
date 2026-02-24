package com.cosmeticshop.cosmetic.Controller;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.CreateProductRequest;
import com.cosmeticshop.cosmetic.Entity.Product;
import com.cosmeticshop.cosmetic.Service.ProductService;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    //tạo sản phẩm mới
    @PostMapping("/create_product")
    public Product createProduct(@RequestBody CreateProductRequest request){
        return productService.createProduct(request);
    }
    
    //Lấy tất cả sản phẩm
    @GetMapping
    public List<Product> getAllProduct(){
        return productService.getAllProducts();
    }

    //lấy sản phẩm theo id
    @GetMapping("/{id}")
    public Product getProductbyId(@PathVariable Long id){

        return productService.getProductById(id);
    }
    
    //tìm kiếm sản phẩm
    @GetMapping("/search")
    public String searchMethod(@RequestParam String query){
        return query;
    }
    
}
