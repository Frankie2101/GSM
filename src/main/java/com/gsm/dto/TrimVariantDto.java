package com.gsm.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

/**
 * DTO for a Trim's variant (e.g., by color and size).
 */
@Data
public class TrimVariantDto {

    private Long trimVariantId;

    @NotBlank(message = "Color Code cannot be blank")
    private String colorCode;

    private String colorName;

    @NotBlank(message = "Size Code cannot be blank")
    private String sizeCode;

    @PositiveOrZero(message = "Net price must be zero or positive")
    private Double netPrice;

    @PositiveOrZero(message = "Tax rate must be zero or positive")
    private Double taxRate;

    private boolean deletable = true;
}