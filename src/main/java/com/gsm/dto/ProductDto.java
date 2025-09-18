package com.gsm.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.gsm.enums.ProductStatus;
import lombok.Data;
import java.util.List;

/**
 * Data Transfer Object for the Product entity.
 * <p>
 * This class is used to transfer product data between the presentation layer (UI)
 * and the service layer. It encapsulates the data received from the product form
 * and also carries data back to the UI for display. It includes Java Bean
 * Validation annotations to enforce data integrity rules.
 */
@Data
public class ProductDto {
    /**
     * The unique identifier of the product. Can be null for a new product.
     */
    private Long productId;

    /**
     * A sequence number generated on the fly for display in UI lists. Not persisted.
     */
    private Long sequenceNumber;

    /**
     * The business code for the product. Must not be blank.
     */
    @NotBlank(message = "Product code cannot be blank")
    @Size(max = 50, message = "Product code must be less than 50 characters")
    private String productCode;

    /**
     * The descriptive name of the product. Must not be blank.
     */
    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 100, message = "Product name must be less than 100 characters")
    private String productName;

    /**
     * The ID of the associated product category. Must not be null.
     */
    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    /**
     * The ID of the associated unit of measure. Must not be null.
     */
    @NotNull(message = "Unit ID cannot be null")
    private Long unitId;

    /**
     * The name of the category, used for display purposes.
     */
    private String categoryName;

    /**
     * The name of the unit, used for display purposes.
     */
    private String unitName;

    /**
     * The season or collection of the product.
     */
    @Size(max = 50, message = "Season must be less than 50 characters")
    private String season;

    /**
     * The current status of the product.
     */
    @NotNull(message = "Status is required")
    private ProductStatus status;

    /**
     * A list of variants associated with this product.
     * The @Valid annotation triggers validation for each {@link ProductVariantDto} object within this list.
     */
    @Valid
    private List<ProductVariantDto> variants;
}