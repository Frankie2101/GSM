package com.gsm.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SaleOrderDetailDto {
    // Thông tin định danh cho một dòng trong bảng (Product + Color)
    private Long productId;
    private String productName;
    private String color;
    private String unitName;

    // Cấu trúc dạng ma trận để chứa số lượng, giá, và ID của từng size
    // Key là Size (e.g., "S", "M", "L"), Value là giá trị tương ứng
    private Map<String, Integer> quantities;
    private Map<String, Double> prices;
    private Map<String, Long> variantIds;
    private Map<String, Integer> shipQuantities;
}
