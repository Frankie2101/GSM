package com.gsm.dto;

import com.gsm.enums.SaleOrderStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DTO for the SaleOrder entity. Carries data for the create/edit form.
 */
@Data
public class SaleOrderDto {
    private Long saleOrderId;
    private Long sequenceNumber;

    @NotBlank(message = "Sale Order Number cannot be blank")
    private String saleOrderNo;

    /**
     * The @DateTimeFormat annotation helps Spring MVC correctly parse the date
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate orderDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotBlank(message = "Ship Date cannot be blank")
    private LocalDate shipDate;

    private String customerPO;

    @NotBlank(message = "Currency Code cannot be blank")
    private String currencyCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotBlank(message = "Production Start Date cannot be blank")
    private LocalDate productionStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotBlank(message = "Production End Date cannot be blank")
    private LocalDate productionEndDate;

    private SaleOrderStatus status;

    @NotNull(message = "Customer is required")
    private Long customerId;

    private String customerName;

    @Valid
    private List<SaleOrderDetailDto> details;

    /**
     * A helper method to provide a formatted date string directly to the view (template).
     * @return The ship date formatted as "MM/dd/yyyy", or an empty string if null.
     */
    public String getFormattedShipDate() {
        if (this.shipDate != null) {
            return this.shipDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }
        return "";
    }

    private boolean deletable = true;
}
