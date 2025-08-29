// File: src/main/java/com/gsm/service/FabricService.java
package com.gsm.service;

import com.gsm.dto.FabricDto;
import java.util.List;

public interface FabricService {
    List<FabricDto> findAll();
    FabricDto findById(Long id);
    FabricDto save(FabricDto fabricDto);
    void deleteByIds(List<Long> ids);
    List<FabricDto> search(String keyword);
}