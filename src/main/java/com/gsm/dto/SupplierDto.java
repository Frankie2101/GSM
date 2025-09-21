package com.gsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * A DTO to carry basic supplier information, often for populating dropdowns or API responses.
 */
@Getter
@Setter
@AllArgsConstructor
public class SupplierDto {
    private Long supplierId;
    private String supplierName;
    private String currencyCode;
}