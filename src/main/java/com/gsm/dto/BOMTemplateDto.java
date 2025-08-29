// File: src/main/java/com/gsm/dto/BOMTemplateDto.java
package com.gsm.dto;

import lombok.Data;
import javax.validation.Valid;
import java.util.List;

@Data
public class BOMTemplateDto {
    private Long bomTemplateId;
    private Long sequenceNumber;

    private String bomTemplateCode;
    private String bomTemplateName;

    private Long productCategoryId;
    private String productCategoryName; // For display on list page

    @Valid
    private List<BOMTemplateDetailDto> details;
}