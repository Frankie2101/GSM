package com.gsm.dto;

import lombok.Data;
import java.util.Map;

/**
 * DTO for a single line in the Sale Order form's detail table.
 * This class uses a "pivoted" structure, where sizes are represented as keys in maps,
 * which is convenient for UI display and data entry.
 */
@Data
public class SaleOrderDetailDto {

    // Identity fields for a pivoted row (Product + Color)
    private Long productId;
    private String productName;
    private String color;
    private String unitName;

    /**
     * A map to hold quantities for different sizes.
     * Key: Size (e.g., "S", "M", "L"). Value: Order quantity for that size.
     */
    private Map<String, Integer> quantities;

    /**
     * A map to hold prices for different sizes.
     * Key: Size. Value: Price for that size.
     */
    private Map<String, Double> prices;

    /**
     * A map to hold the ProductVariant ID for each size.
     * Key: Size. Value: The corresponding productVariantId.
     */
    private Map<String, Long> variantIds;


    /**
     * A map to hold the shipped quantity for each size.
     * Key: Size. Value: The shipped quantity.
     */
    private Map<String, Integer> shipQuantities;
}
