package com.cosmeticshop.cosmetic.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cosmeticshop.cosmetic.Entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByOrderDateDescIdDesc(Long userId);

	List<Order> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);

	@Query("""
			SELECT oi.product.id, oi.product.name, SUM(oi.quantity), SUM(oi.quantity * oi.price)
			FROM OrderItem oi
			WHERE oi.order.orderDate BETWEEN :startDate AND :endDate
			  AND oi.order.status = com.cosmeticshop.cosmetic.Entity.Order.Status.DELIVERED
			GROUP BY oi.product.id, oi.product.name
			ORDER BY SUM(oi.quantity) DESC
		""")
	List<Object[]> findTopSellingProducts(
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			Pageable pageable);

}
