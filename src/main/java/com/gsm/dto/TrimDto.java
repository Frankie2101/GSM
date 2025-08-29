package com.gsm.dto;

import lombok.Data;
import javax.validation.Valid;
import java.util.List;

@Data
public class TrimDto {
    private Long trimId;
    private Long sequenceNumber;

    private String trimCode;
    private String trimName;

    private Long unitId;
    private Long supplierId;

    // For display purposes on list page
    private String unitName;
    private String supplierName;

    private String technicalReference;

    @Valid
    private List<TrimVariantDto> variants;
}
