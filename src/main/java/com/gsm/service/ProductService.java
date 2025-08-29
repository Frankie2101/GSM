package com.gsm.service;

import com.gsm.dto.ProductDto;
import java.util.List;

public interface ProductService {
    List<ProductDto> findAll();

    ProductDto findById(Long id);

    ProductDto save(ProductDto productDto);

    List<ProductDto> search(String keyword, String categoryName);
    void deleteByIds(List<Long> ids);

}