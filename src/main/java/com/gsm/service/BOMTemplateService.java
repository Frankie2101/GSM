// File: src/main/java/com/gsm/service/BOMTemplateService.java
package com.gsm.service;

import com.gsm.dto.BOMTemplateDto;
import java.util.List;

public interface BOMTemplateService {
    List<BOMTemplateDto> findAll();
    BOMTemplateDto findById(Long id);
    BOMTemplateDto save(BOMTemplateDto dto);
    void deleteByIds(List<Long> ids);
    List<BOMTemplateDto> search(String keyword);
}
