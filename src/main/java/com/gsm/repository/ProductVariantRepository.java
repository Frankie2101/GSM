package com.gsm.repository;

import com.gsm.controller.api.SaleOrderApiController.ColorInfo;
import com.gsm.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProduct_ProductId(Long productId);
    List<ProductVariant> findByProduct_ProductIdAndColor(Long productId, String color);

    // SỬA LẠI: Trả về dữ liệu thô thay vì DTO
    @Query("SELECT DISTINCT v.color, v.color FROM ProductVariant v WHERE v.product.productId = :productId ORDER BY v.color")
    List<Object[]> findDistinctColorsByProductId(@Param("productId") Long productId);

    @Transactional
    void deleteByProduct_ProductId(Long productId);
}
