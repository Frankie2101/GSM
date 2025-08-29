package com.gsm.dto;

import lombok.Data;

@Data
public class TrimVariantDto {
    private Long trimVariantId;
    private String colorCode;
    private String colorName;
    private String sizeCode;
    private Double netPrice;
    private Double taxRate;
}