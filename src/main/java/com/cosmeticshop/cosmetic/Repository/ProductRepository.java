package com.cosmeticshop.cosmetic.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cosmeticshop.cosmetic.Entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
