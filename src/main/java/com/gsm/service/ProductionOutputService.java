package com.gsm.service;

import com.gsm.dto.ProductionOutputDto;
import java.time.LocalDate;
import java.util.List;

public interface ProductionOutputService {
    List<ProductionOutputDto> findAll();
    // YÊU CẦU MỚI: Cập nhật chữ ký hàm search
    List<ProductionOutputDto> search(String keyword, LocalDate outputDateFrom, LocalDate outputDateTo, String department, String productionLine);
    void deleteByIds(List<Long> ids);
}
