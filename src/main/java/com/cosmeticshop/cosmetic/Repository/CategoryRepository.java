package com.cosmeticshop.cosmetic.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cosmeticshop.cosmetic.Entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
