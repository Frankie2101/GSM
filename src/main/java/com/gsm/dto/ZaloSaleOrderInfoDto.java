package com.gsm.dto;

import java.util.List;

public class ZaloSaleOrderInfoDto {
    private Long saleOrderId;
    private List<ZaloStyleColorDto> stylesAndColors;

    public ZaloSaleOrderInfoDto(Long saleOrderId, List<ZaloStyleColorDto> stylesAndColors) {
        this.saleOrderId = saleOrderId;
        this.stylesAndColors = stylesAndColors;
    }

    // Getters and Setters
    public Long getSaleOrderId() { return saleOrderId; }
    public void setSaleOrderId(Long saleOrderId) { this.saleOrderId = saleOrderId; }
    public List<ZaloStyleColorDto> getStylesAndColors() { return stylesAndColors; }
    public void setStylesAndColors(List<ZaloStyleColorDto> stylesAndColors) { this.stylesAndColors = stylesAndColors; }
}