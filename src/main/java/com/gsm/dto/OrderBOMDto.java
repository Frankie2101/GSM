package com.gsm.dto;

import lombok.Data;
import javax.validation.Valid;
import java.util.List;

/**
 * Represents the main Order BOM object, containing header information
 * and a list of detail lines (materials).
 */
@Data
public class OrderBOMDto {
    private Long orderBOMId;
    private Long saleOrderId;
    private String saleOrderNo;
    private Long bomTemplateId;
    private String bomTemplateName;
    private int sequenceNumber;

    @Valid
    private List<OrderBOMDetailDto> details;
}