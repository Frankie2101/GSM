package com.gsm.dto;

import lombok.Data;
import javax.validation.Valid;
import java.util.List;

@Data
public class OrderBOMDto {
    private Long orderBOMId;
    private Long saleOrderId;
    private String saleOrderNo;
    private Long bomTemplateId;
    private String bomTemplateName; // Thêm để hiển thị

    @Valid
    private List<OrderBOMDetailDto> details;
}