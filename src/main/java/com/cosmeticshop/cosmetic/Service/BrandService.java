package com.cosmeticshop.cosmetic.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateBrandRequest;
import com.cosmeticshop.cosmetic.Entity.Brand;
import com.cosmeticshop.cosmetic.Exception.ResourceNotFoundException;
import com.cosmeticshop.cosmetic.Repository.BrandRepository;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository){
        this.brandRepository = brandRepository;
    }

    public Brand createBrand(CreateBrandRequest request){

        Brand brand= new Brand();
        brand.setName(request.getName());
        brand.setOrigin(request.getOrigin());

        return brandRepository.save(brand);
    }

    public List<Brand> getAllBrands(){
        return brandRepository.findAll();
    }
    
    public Brand getBrandById(Long id){
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand không tồn tại với ID: " + id));
    }

    public void deleteBrandById(Long id){

        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Brand không tồn tại với ID: " + id));

        brandRepository.delete(brand);
    }
}
