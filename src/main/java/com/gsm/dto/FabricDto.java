package com.gsm.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Data Transfer Object for transferring Fabric data between layers.
 * Includes validation annotations to ensure data integrity from the UI.
 */
@Data
public class FabricDto {
    private Long fabricId;
    private Long sequenceNumber;
    private boolean activeFlag = true;

    @NotBlank(message = "Fabric Code cannot be blank")
    private String fabricCode;

    @NotBlank(message = "Fabric Name cannot be blank")
    private String fabricName;

    private String fabricType;
    private String construction;

    @NotNull(message = "Unit is required")
    private Long unitId;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Material Group is required")
    private Long materialGroupId;

    // For display purposes only, not submitted from form
    private String materialGroupName;
    private String supplierName;
    private String unitName;

    private String yarnCount;
    private String fabricContent;
    private String finishing;

    /**
     * The list of color variants for this fabric.
     * @Valid triggers nested validation on each FabricColorDto object.
     */
    @Valid
    private List<FabricColorDto> fabricColors;
}