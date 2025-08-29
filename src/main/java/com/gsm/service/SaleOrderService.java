package com.gsm.service;

import com.gsm.dto.SaleOrderDto;
import java.util.List;

public interface SaleOrderService {
    List<SaleOrderDto> findAll();
    SaleOrderDto findById(Long id);
    SaleOrderDto save(SaleOrderDto dto);
    void deleteByIds(List<Long> ids);
    List<SaleOrderDto> search(String keyword);
}