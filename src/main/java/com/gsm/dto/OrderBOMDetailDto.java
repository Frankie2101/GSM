package com.gsm.dto;

import lombok.Data;

/**
 * Represents a single line item within an Order BOM, corresponding to one material.
 * This DTO is used to transfer data between the view and the controller for Order BOM details.
 */
@Data
public class OrderBOMDetailDto {
    private Long orderBOMDetailId;
    private Integer seq;
    private Long materialGroupId;
    private String materialGroupName;
    private String materialType; // "FA" for Fabric, "TR" for Trim

    private Long fabricId;
    private Long trimId;

    /**
     * Fields that are displayed and can be edited by the user.
     */
    private String materialCode;
    private String materialName;
    private String uom;
    private Long supplierId;
    private String supplierName;
    private Double price;
    private String currency;
    private Double usageValue;
    private Double waste;


    /**
     * Color and size information for the material.
     */
    private String colorCode;
    private String colorName;
    private String size;

    /**
     * Quantity-related fields.
     */
    private int soQty; // Total quantity from the Sale Order
    private Double demandQty; // Required material quantity
    private Double inventoryQty; // Quantity on hand
    private Double purchaseQty; // Quantity to be purchased

    private boolean isInPo = false;
}