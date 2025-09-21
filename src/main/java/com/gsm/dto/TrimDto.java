package com.gsm.dto;

import lombok.Data;
import javax.validation.Valid;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for the Trim entity. Includes validation rules.
 */
@Data
public class TrimDto {
    private Long trimId;
    private Long sequenceNumber;

    @NotBlank(message = "Trim Code cannot be blank")
    private String trimCode;

    @NotBlank(message = "Trim Name cannot be blank")
    private String trimName;

    @NotNull(message = "Unit is required")
    private Long unitId;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Material Group is required")
    private Long materialGroupId;

    // For display purposes on list page
    private String materialGroupName;
    private String unitName;
    private String supplierName;

    @Size(max = 1000)
    private String technicalReference;

    @Valid
    private List<TrimVariantDto> variants;
}
