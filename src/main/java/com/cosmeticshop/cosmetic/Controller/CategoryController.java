package com.cosmeticshop.cosmetic.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cosmeticshop.cosmetic.Dto.CreateCategoryRequest;
import com.cosmeticshop.cosmetic.Entity.Category;
import com.cosmeticshop.cosmetic.Service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){

        this.categoryService = categoryService;
    }

    
    @PostMapping
    public Category createCategory(@RequestBody CreateCategoryRequest request){

        return categoryService.createCategory(request);
    }

    
    @GetMapping
    public List<Category> getAllCategories(){
        return categoryService.getAllCategories();
    }

    
    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Long id){

        return categoryService.getCategoryById(id);
    }


    
    @DeleteMapping("/{id}")
    public void deletecategory(@PathVariable Long id){

        categoryService.deletecategory(id);
    }
}
