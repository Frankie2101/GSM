package com.gsm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for a ProductVariant.
 * <p>
 * Represents a specific variation of a product (e.g., by color and size).
 * It is typically used as a nested object within a {@link ProductDto}.
 */
@Data
public class ProductVariantDto {

    /**
     * The unique identifier of the product variant. Can be null for a new variant.
     */
    private Long productVariantId;

    /**
     * The color of the variant.
     */
    @NotBlank(message = "Color cannot be blank")
    @Size(max = 50)
    private String color;

    /**
     * The size of the variant. Can contain multiple sizes separated by commas (e.g., "S, M, L").
     */
    @NotBlank(message = "Size cannot be blank")
    @Size(max = 20)
    private String size;

    /**
     * The Stock Keeping Unit. Should be unique for each variant.
     */
    @NotBlank(message = "SKU cannot be blank")
    @Size(max = 100)
    private String sku;

    /**
     * The selling price of the variant. Must be a positive number or zero.
     */
    @NotNull(message = "Price cannot be null")
    @PositiveOrZero(message = "Price must be zero or positive")
    private Double price;

    /**
     * The currency code for the price, e.g., "VND".
     */
    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency;
}