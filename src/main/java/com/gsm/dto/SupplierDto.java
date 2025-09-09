package com.gsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SupplierDto {
    private Long supplierId;
    private String supplierName;
    private String currencyCode;
}