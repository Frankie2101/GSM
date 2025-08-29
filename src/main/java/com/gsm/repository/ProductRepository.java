package com.gsm.repository;

import com.gsm.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Kiểm tra xem một ProductCode đã tồn tại hay chưa.
     * Spring Data JPA sẽ tự động tạo câu lệnh query từ tên phương thức này.
     * @param productCode Mã sản phẩm cần kiểm tra.
     * @return true nếu tồn tại, false nếu ngược lại.
     */
    boolean existsByProductCode(String productCode);

    /**
     * Tìm một sản phẩm dựa trên ProductCode.
     * @param productCode Mã sản phẩm cần tìm.
     * @return một Optional chứa Product nếu tìm thấy.
     */
    Optional<Product> findByProductCode(String productCode);

    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryName IS NULL OR p.productCategory.categoryName = :categoryName)")
    List<Product> searchProducts(@Param("keyword") String keyword, @Param("categoryName") String categoryName);
}