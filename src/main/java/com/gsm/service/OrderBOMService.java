package com.gsm.service;

import com.gsm.dto.OrderBOMDto;

public interface OrderBOMService {
    // Tìm OrderBOM đã có hoặc tạo DTO mới cho form
    OrderBOMDto findOrCreateBySaleOrderId(Long saleOrderId);

    // Tạo bản xem trước BOM từ template
    OrderBOMDto generatePreviewFromTemplate(Long saleOrderId, Long bomTemplateId);

    // Lưu OrderBOM
    OrderBOMDto save(OrderBOMDto dto);
}