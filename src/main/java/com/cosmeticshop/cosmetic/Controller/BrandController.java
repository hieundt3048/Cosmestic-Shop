package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.CreateBrandRequest;
import com.cosmeticshop.cosmetic.Entity.Brand;
import com.cosmeticshop.cosmetic.Service.BrandService;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService){

        this.brandService = brandService;
    }

    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Brand createBrand(@RequestBody CreateBrandRequest request){
        return brandService.createBrand(request);
    }

    
    @GetMapping
    public List<Brand> getAllBrands(){
        return brandService.getAllBrands();
    }

    
    @GetMapping("/{id}")
    public Brand getBrandById(@PathVariable Long id){

        return brandService.getBrandById(id);
    }

    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBrandById(@PathVariable Long id){
        brandService.deleteBrandById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Brand updateBrand(@PathVariable Long id, @RequestBody CreateBrandRequest request) {
        return brandService.updateBrandById(id, request);
    }
}
