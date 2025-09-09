package com.gsm.service;

import com.gsm.dto.OrderBOMDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface OrderBOMService {
    // ==========================================================
    // === PHƯƠNG THỨC MỚI ĐỂ LẤY DANH SÁCH BOM ===
    // ==========================================================
    @Transactional(readOnly = true)
    List<OrderBOMDto> findAll();

    // Tìm OrderBOM đã có hoặc tạo DTO mới cho form
    OrderBOMDto findOrCreateBySaleOrderId(Long saleOrderId);

    // Tạo bản xem trước BOM từ template
    OrderBOMDto generatePreviewFromTemplate(Long saleOrderId, Long bomTemplateId);

    // Lưu OrderBOM
    OrderBOMDto save(OrderBOMDto dto);

    Map<String, Object> saveAndGeneratePOs(OrderBOMDto bomDtoFromForm);

}