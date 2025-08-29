package com.gsm.dto;

import lombok.Data;

@Data
public class FabricColorDto {
    private Long fabricColorId;
    private String color;
    private String colorName;
    private String width;
    private Double netPrice;
    private Double taxPercent;
}