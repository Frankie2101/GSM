package com.gsm.repository;

import com.gsm.model.MaterialGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialGroupRepository extends JpaRepository<MaterialGroup, Long> {

    // Spring Data JPA sẽ tự động tạo ra các phương thức tìm kiếm dựa trên tên.
    // Ví dụ, tìm các MaterialGroup theo loại (FA hoặc TR).
    List<MaterialGroup> findByMaterialType(String materialType);
}