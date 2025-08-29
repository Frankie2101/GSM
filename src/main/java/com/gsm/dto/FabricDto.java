// File: src/main/java/com/gsm/dto/FabricDto.java
package com.gsm.dto;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;
// ... các import khác ...

@Data
public class FabricDto {
    private Long fabricId;
    private Long sequenceNumber;
    private boolean activeFlag = true;
    private String fabricCode;
    private String fabricName;
    private String fabricType;
    private String construction;

    private Long unitId;
    private Long supplierId;

    // THÊM 2 TRƯỜNG NÀY VÀO
    private String supplierName;
    private String unitName;

    private String yarnCount;
    private String fabricContent;
    private String finishing;

    @Valid
    private List<FabricColorDto> fabricColors;
}