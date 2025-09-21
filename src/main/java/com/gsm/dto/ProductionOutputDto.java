package com.gsm.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for transferring Production Output data.
 * Used for both displaying existing records and receiving new ones from APIs.
 */
@Data
public class ProductionOutputDto {
    private Long productionOutputId;
    private Long sequenceNumber;

    @NotNull(message = "Sale Order ID is required")
    private Long saleOrderId;
    private String saleOrderNo;

    @NotBlank(message = "Style cannot be blank")
    private String style;

    @NotBlank(message = "Color cannot be blank")
    private String color;
    private String department;
    private String productionLine;

    @NotNull(message = "Output Date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate outputDate;

    @NotNull(message = "Output Quantity is required")
    private Integer outputQuantity;

    // For display purposes
    private String createdBy;

    // ID of the user submitting the output
    private Long userId;
}