package com.gsm.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
/**
 * Data Transfer Object for a Fabric's color variant.
 */
@Data
public class FabricColorDto {

    private Long fabricColorId;

    @NotBlank(message = "Color code cannot be blank")
    private String color;

    private String colorName;
    private String width;

    @PositiveOrZero(message = "Net price must be zero or positive")
    private Double netPrice;

    @PositiveOrZero(message = "Tax percent must be zero or positive")
    private Double taxPercent;

    private boolean deletable = true;
}