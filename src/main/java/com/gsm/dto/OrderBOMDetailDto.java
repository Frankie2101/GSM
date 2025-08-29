package com.gsm.dto;

import lombok.Data;

@Data
public class OrderBOMDetailDto {
    private Long orderBOMDetailId;
    private Integer seq;
    private Long materialGroupId;
    private String materialGroupName;
    private String materialType; // "FA" hoặc "TR"

    // Giữ lại ID của NPL gốc từ template
    private Long fabricId;
    private Long trimId;

    // Các trường hiển thị và cho phép sửa
    private String materialCode;
    private String materialName;
    private String uom;
    private String supplier;
    private Double price;
    private String currency;
    private Double usageValue;
    private Double waste;


    // === THÊM CÁC TRƯỜNG BỊ THIẾU VÀO ĐÂY ===
    private String colorCode;
    private String colorName;
    private String size;

    // Sửa lại kiểu dữ liệu thành Double để có 4 số thập phân
    private int soQty;
    private Double demandQty;
    private Double inventoryQty;
    private Double purchaseQty;
}