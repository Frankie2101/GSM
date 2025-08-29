package com.gsm.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.gsm.enums.ProductStatus;
import lombok.Data;
import java.util.List;

@Data
public class ProductDto {
    private Long productId;
    private Long sequenceNumber;

    @NotBlank(message = "Product code cannot be blank")
    @Size(max = 50, message = "Product code must be less than 50 characters")
    private String productCode;

    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 100, message = "Product name must be less than 100 characters")
    private String productName;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    @NotNull(message = "Unit ID cannot be null")
    private Long unitId;

    private String categoryName;
    private String unitName;

    @Size(max = 50, message = "Season must be less than 50 characters")
    private String season;

    @NotNull(message = "Status is required")
    private ProductStatus status;

    @Valid // Yêu cầu Spring kiểm tra validation cho các đối tượng trong danh sách này
    private List<ProductVariantDto> variants;
}