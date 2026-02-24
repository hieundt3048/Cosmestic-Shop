package com.cosmeticshop.cosmetic.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.CreateCategoryRequest;
import com.cosmeticshop.cosmetic.Entity.Category;
import com.cosmeticshop.cosmetic.Exception.ResourceNotFoundException;
import com.cosmeticshop.cosmetic.Repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(CreateCategoryRequest request){

        Category category = new Category();
        category.setName(request.getName());

        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id){

        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại với ID: " + id));
    }

    public void deletecategory(Long id){

        Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại với ID: " + id));

        categoryRepository.delete(category);
    }
}
