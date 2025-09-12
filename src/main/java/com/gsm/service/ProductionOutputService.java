package com.gsm.service;

import com.gsm.dto.ProductionOutputDto;
import java.time.LocalDate;
import java.util.List;

public interface ProductionOutputService {

    // Thêm lại phương thức findAll() để khớp với interface của bạn
    List<ProductionOutputDto> findAll();

    List<ProductionOutputDto> search(String keyword, LocalDate outputDateFrom, LocalDate outputDateTo, String department, String productionLine);

    void deleteByIds(List<Long> ids);

    ProductionOutputDto save(ProductionOutputDto dto);

    ProductionOutputDto findById(Long id);
}