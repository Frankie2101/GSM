package com.gsm.service;

import com.gsm.dto.BOMTemplateDto;
import java.util.List;

/**
 * Defines the contract for business operations related to BOM Templates.
 */
public interface BOMTemplateService {
    List<BOMTemplateDto> findAll();
    BOMTemplateDto findById(Long id);
    BOMTemplateDto save(BOMTemplateDto dto);
    void deleteByIds(List<Long> ids);
    List<BOMTemplateDto> search(String keyword);
}
