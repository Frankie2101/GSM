package com.gsm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductVariantDto {
    private Long productVariantId;

    @NotBlank(message = "Color cannot be blank")
    @Size(max = 50)
    private String color;

    @NotBlank(message = "Size cannot be blank")
    @Size(max = 20)
    private String size;

    @NotBlank(message = "SKU cannot be blank")
    @Size(max = 100)
    private String sku;

    @NotNull(message = "Price cannot be null")
    @PositiveOrZero(message = "Price must be zero or positive")
    private Double price;

    // Thêm vào dưới thuộc tính price
    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency;
}