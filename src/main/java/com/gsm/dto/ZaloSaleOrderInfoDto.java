package com.gsm.dto;

import java.util.List;

/**
 * A DTO that acts as a wrapper to return information about a Sale Order to the Zalo Mini App.
 * It is the response object for the GET /api/zalo/sale-orders/{soNo}/styles endpoint.
 */
public class ZaloSaleOrderInfoDto {

    /**
     * The internal database ID of the sale order.
     */
    private Long saleOrderId;

    /**
     * A list containing all the valid style and color combinations for this order.
     */
    private List<ZaloStyleColorDto> stylesAndColors;

    public ZaloSaleOrderInfoDto(Long saleOrderId, List<ZaloStyleColorDto> stylesAndColors) {
        this.saleOrderId = saleOrderId;
        this.stylesAndColors = stylesAndColors;
    }

    public Long getSaleOrderId() { return saleOrderId; }
    public void setSaleOrderId(Long saleOrderId) { this.saleOrderId = saleOrderId; }
    public List<ZaloStyleColorDto> getStylesAndColors() { return stylesAndColors; }
    public void setStylesAndColors(List<ZaloStyleColorDto> stylesAndColors) { this.stylesAndColors = stylesAndColors; }
}