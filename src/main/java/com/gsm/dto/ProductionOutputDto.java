package com.gsm.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class ProductionOutputDto {
    private Long productionOutputId;
    private Long sequenceNumber; // Để đánh số thứ tự trên UI

    // Thông tin từ SaleOrder liên quan
    private Long saleOrderId;
    private String saleOrderNo;

    // Thông tin chính của ProductionOutput
    private String style;
    private String color;
    private String department;
    private String productionLine;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate outputDate;

    private Integer outputQuantity;
    private String createdBy;
    private Long userId;
}