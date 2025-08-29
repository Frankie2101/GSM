// File: src/main/java/com/gsm/repository/SupplierRepository.java
package com.gsm.repository;

import com.gsm.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Spring Data JPA tự động cung cấp các phương thức CRUD cơ bản.
    // Chúng ta có thể thêm các phương thức truy vấn tùy chỉnh ở đây nếu cần trong tương lai.
    // Ví dụ: tìm nhà cung cấp theo mã.
    Optional<Supplier> findBySupplierCode(String supplierCode);
}
