package com.gsm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Data Transfer Object for the PurchaseOrder entity.
 * This class carries data for creating, updating, and displaying purchase orders.
 * It includes validation annotations to ensure data integrity.
 */
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderDto {

    private Long purchaseOrderId;

    @NotBlank(message = "Purchase Order Number cannot be blank")
    private String purchaseOrderNo;

    private String supplierName;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "PO Date is required")
    private LocalDate poDate;
    private String currencyCode;
    private String deliveryTerm;
    private String paymentTerm;
    private LocalDate arrivalDate;
    private String status;

    /**
     * The list of detail lines for this purchase order.
     * @Valid ensures that validation rules on each {@link PurchaseOrderDetailDto} are also checked.
     */
    @Valid
    @NotNull
    private List<PurchaseOrderDetailDto> details;

    private Double totalAmount;

    /**
     * A custom constructor designed for use with JPA Constructor Expressions.
     * This allows the repository to fetch data directly into this DTO
     */
    public PurchaseOrderDto(Long purchaseOrderId, String purchaseOrderNo, String supplierName, LocalDate poDate, String currencyCode, String deliveryTerm, String paymentTerm, LocalDate arrivalDate, String status) {
        this.purchaseOrderId = purchaseOrderId;
        this.purchaseOrderNo = purchaseOrderNo;
        this.supplierName = supplierName;
        this.poDate = poDate;
        this.currencyCode = currencyCode;
        this.deliveryTerm = deliveryTerm;
        this.paymentTerm = paymentTerm;
        this.arrivalDate = arrivalDate;
        this.status = status;
    }
}
