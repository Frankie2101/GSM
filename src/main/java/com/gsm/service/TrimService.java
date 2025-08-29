package com.gsm.service;

import com.gsm.dto.TrimDto;
import java.util.List;

public interface TrimService {
    List<TrimDto> findAll();
    TrimDto findById(Long id);
    TrimDto save(TrimDto trimDto);
    void deleteByIds(List<Long> ids);
    List<TrimDto> search(String keyword);
}