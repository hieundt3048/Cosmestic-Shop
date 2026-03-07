package com.cosmeticshop.cosmetic.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cosmeticshop.cosmetic.Entity.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    List<Voucher> findAllByOrderByCreatedAtDesc();

    Optional<Voucher> findByCodeIgnoreCase(String code);
}
