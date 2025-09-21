package com.gsm.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

/**
 * DTO for a single detail line within a {@link BOMTemplateDto}.
 */
@Data
public class BOMTemplateDetailDto {

    private Long bomTemplateDetailId;
    private Integer seq;

    private Long materialGroupId;

    @NotBlank(message = "Material Type (FA/TR) is required")
    private String rmType; // Raw Material Type (FA = Fabric, TR = Trim)

    @NotNull(message = "Usage value is required")
    @PositiveOrZero(message = "Usage must be a positive number or zero")
    private Double usageValue;

    @NotNull(message = "Waste percentage is required")
    @PositiveOrZero(message = "Waste must be a positive number or zero")
    private Double waste;

    @NotNull(message = "A material must be selected")
    private Long rmId;

    // The following fields are primarily for display or retrieval, not direct submission.
    private String rmCode;
    private String rmName;
    private String unitName;

    /**
     * Helper method for Mustache templates to check if the material type is Fabric.
     * Allows for conditional rendering in the template with `{{#isFa}}...{{/isFa}}`.
     * @return true if rmType is "FA".
     */
    public boolean isFa() {
        return "FA".equals(rmType);
    }

    /**
     * Helper method for Mustache templates to check if the material type is Trim.
     * Allows for conditional rendering in the template with `{{#isTr}}...{{/isTr}}`.
     * @return true if rmType is "TR".
     */
    public boolean isTr() {
        return "TR".equals(rmType);
    }
}
