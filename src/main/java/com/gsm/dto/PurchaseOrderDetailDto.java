package com.gsm.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

/**
 * DTO for a single line item within a {@link PurchaseOrderDto}.
 * Represents a specific raw material being purchased.
 */
@Data
public class PurchaseOrderDetailDto {
    private Long purchaseOrderDetailId;

    /**
     * The ID of the originating OrderBOMDetail line.
     * This provides traceability back to the original demand calculation.
     */
    @NotNull(message = "Order BOM Detail ID is required")
    private Long orderBOMDetailId;

    // --- Fields for reference and display ---
    private Long fabricId;
    private Long trimId;
    private String materialType;
    private String materialCode;
    private String materialName;
    private String colorCode;
    private String size;
    private String uom;

    /**
     * The quantity of the material being purchased.
     */
    @NotNull(message = "Purchase Quantity is required")
    @PositiveOrZero(message = "Purchase Quantity must not be negative")
    private Double purchaseQuantity;

    /**
     * The price per unit for the material.
     */
    @NotNull(message = "Net Price is required")
    @PositiveOrZero(message = "Net Price must not be negative")
    private Double netPrice;

    /**
     * The tax rate (e.g., VAT) as a percentage.
     */
    private Double taxRate;

    /**
     * The quantity that has been physically received.
     */
    private Double receivedQuantity;

    /**
     * The calculated total amount for this line item.
     */
    private Double lineAmount;
}