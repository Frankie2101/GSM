package com.gsm.dto;

import com.gsm.enums.SaleOrderStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat; // THÊM IMPORT NÀY

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class SaleOrderDto {
    private Long saleOrderId;
    private Long sequenceNumber;
    private String saleOrderNo;

    // THÊM ANNOTATION ĐỂ SỬA LỖI
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate orderDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate shipDate;

    private String customerPO;
    private String currencyCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate productionStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate productionEndDate;

    private SaleOrderStatus status;

    private Long customerId;
    private String customerName;

    @Valid
    private List<SaleOrderDetailDto> details;

    // THÊM MỚI: Phương thức để định dạng ngày tháng cho view
    public String getFormattedShipDate() {
        if (this.shipDate != null) {
            return this.shipDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }
        return "";
    }
}
