package com.gsm.dto;

import lombok.Data;

@Data
public class ZaloSaleOrderDetailDto {
    // Thông tin định danh cho một nhóm
    private String style; // Tên sản phẩm
    private String color;

    // Thông tin tổng hợp cho nhóm đó
    private Integer totalOrderQty;
    private Integer totalShippedQty;
}

