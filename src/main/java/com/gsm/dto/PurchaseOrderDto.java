package com.gsm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderDto {

    private Long purchaseOrderId;
    private String purchaseOrderNo;
    private String supplierName;
    private Long supplierId; // <<-- LỖI ĐÃ ĐƯỢC SỬA TẠI ĐÂY: Bổ sung trường còn thiếu
    private LocalDate poDate;
    private String currencyCode;
    private String deliveryTerm;
    private String paymentTerm;
    private LocalDate arrivalDate;
    private String status;
    private List<PurchaseOrderDetailDto> details;
    private Double totalAmount;


    // Constructor dùng để mapping trong Service
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
