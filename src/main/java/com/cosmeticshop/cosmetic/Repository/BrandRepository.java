package com.cosmeticshop.cosmetic.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cosmeticshop.cosmetic.Entity.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
}
