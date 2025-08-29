package com.gsm.dto;

import lombok.Data;

@Data
public class BOMTemplateDetailDto {
    private Long bomTemplateDetailId;
    private Integer seq;
    private String rmType;
    private Double usageValue;
    private Double waste;

    private Long rmId;
    private String rmCode;
    private String rmName;

    // THÊM MỚI: Trường để chứa tên Unit
    private String unitName;

    // --- THÊM MỚI: Các phương thức helper cho Mustache ---
    public boolean isFa() {
        return "FA".equals(rmType);
    }

    public boolean isTr() {
        return "TR".equals(rmType);
    }
}
