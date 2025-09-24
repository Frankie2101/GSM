package com.gsm.service;

import com.gsm.dto.OrderBOMDto;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

/**
 * Defines the contract for business operations related to the Order BOM.
 */
public interface OrderBOMService {
    @Transactional(readOnly = true)
    List<OrderBOMDto> findAll();
    OrderBOMDto findOrCreateBySaleOrderId(Long saleOrderId);
    OrderBOMDto generatePreviewFromTemplate(Long saleOrderId, Long bomTemplateId);
    OrderBOMDto save(OrderBOMDto dto);
    Map<String, Object> saveAndGeneratePOs(OrderBOMDto bomDtoFromForm);
}