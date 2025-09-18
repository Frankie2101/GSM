// File: src/main/java/com/gsm/dto/BOMTemplateDto.java
package com.gsm.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Data Transfer Object for the BOMTemplate entity.
 * Carries data for the create/edit form and includes validation rules.
 */
@Data
public class BOMTemplateDto {
    private Long bomTemplateId;
    private Long sequenceNumber;

    @NotBlank(message = "Template Code cannot be blank")
    @Size(max = 100)
    private String bomTemplateCode;

    @NotBlank(message = "Template Name cannot be blank")
    @Size(max = 100)
    private String bomTemplateName;

    @NotNull(message = "Product Category is required")
    private Long productCategoryId;

    // For display purposes only on the list page.
    private String productCategoryName;

    @Valid
    private List<BOMTemplateDetailDto> details;
}