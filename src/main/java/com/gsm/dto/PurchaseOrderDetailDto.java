package com.gsm.dto;

import lombok.Data;

@Data // Dùng @Data thay vì các annotation riêng lẻ
public class PurchaseOrderDetailDto {
    private Long purchaseOrderDetailId;
    private Long orderBOMDetailId;
    private Long fabricId;
    private Long trimId;
    private String materialType;
    private String materialCode;
    private String materialName;
    private String colorCode;
    private String size;
    private String uom;
    private Double purchaseQuantity;
    private Double netPrice;
    private Double taxRate;
    private Double receivedQuantity;
    private Double lineAmount;
}