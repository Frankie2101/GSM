package com.gsm.service;

import com.gsm.dto.ProductDto;
import com.gsm.dto.ProductVariantDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.Product;
import com.gsm.model.ProductCategory;
import com.gsm.model.ProductVariant;
import com.gsm.model.Unit;
import com.gsm.repository.ProductCategoryRepository;
import com.gsm.repository.ProductRepository;
import com.gsm.repository.ProductVariantRepository;
import com.gsm.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The concrete implementation of the {@link ProductService} interface.
 * This class contains the core business logic for managing products. It coordinates
 * interactions between the controllers and the repositories layer.
 *
 * @author ThanhDX
 * @version 1.0.0
 */
@Service
public class ProductServiceImpl implements ProductService {

    // Dependencies for data access
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final UnitRepository unitRepository;


    /**
     * Constructs the service with all required repository dependencies.
     *
     * @param productRepository        Repository for Product data.
     * @param productVariantRepository Repository for ProductVariant data.
     * @param productCategoryRepository Repository for ProductCategory data.
     * @param unitRepository           Repository for Unit data.
     */
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              ProductVariantRepository productVariantRepository,
                              ProductCategoryRepository productCategoryRepository,
                              UnitRepository unitRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.unitRepository = unitRepository;
    }

    /**
     * {@inheritDoc}
     * <p><b>Use Case:</b> This method is called when a user navigates to the product detail page
     * to view or edit an existing product's information. It fetches all necessary data to populate the form.
     */
    @Override
    @Transactional(readOnly = true) //The transaction is set to read-only for performance optimization.
    public ProductDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertEntityToDto(product);
    }

    /**
     * {@inheritDoc}
     * <p><b>Use Case:</b> This is used to populate the main product list view when the user
     * first navigates to the product management page without any search criteria.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        AtomicInteger index = new AtomicInteger(1); // *Uses an AtomicInteger to generate a client-side sequence number
        return productRepository.findAll().stream()
                .map(product -> {
                    ProductDto dto = convertEntityToDtoSimple(product);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p><b>Use Case:</b> This is the core logic executed when a user clicks the "Save" button
     * on the product form. It handles both the creation of new products and the updating of existing ones.
     */
    @Override
    @Transactional
    public ProductDto save(ProductDto dto) {
        // STEP 1: Pre-process the variant DTO list to expand comma-separated sizes.
        List<ProductVariantDto> processedVariants = new ArrayList<>();
        if (dto.getVariants() != null) {
            for (ProductVariantDto variantDtoFromForm : dto.getVariants()) {
                if (variantDtoFromForm.getSize() == null || variantDtoFromForm.getSize().trim().isEmpty()) {
                    continue; // Skip if the size field is empty
                }

                String[] sizes = variantDtoFromForm.getSize().split("\\s*,\\s*");
                boolean isFirstSize = true;

                for (String size : sizes) {
                    if (size.trim().isEmpty()) continue;

                    ProductVariantDto processedDto = new ProductVariantDto();
                    processedDto.setColor(variantDtoFromForm.getColor());
                    processedDto.setPrice(variantDtoFromForm.getPrice());
                    processedDto.setCurrency(variantDtoFromForm.getCurrency());
                    processedDto.setSize(size.trim());

                    if (isFirstSize) {
                        // First size keeps the original ID for UPDATE operations.
                        processedDto.setProductVariantId(variantDtoFromForm.getProductVariantId());
                        isFirstSize = false;
                    } else {
                        // Subsequent sizes are new records, so they have no ID.
                        processedDto.setProductVariantId(null);
                    }
                    processedVariants.add(processedDto);
                }
            }
        }
        dto.setVariants(processedVariants); // Replace original variants with the processed list.

        // STEP 2: Perform the synchronization logic using the processed variant list.
        Product product = (dto.getProductId() != null)
                ? productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()))
                : new Product();

        mapDtoToEntity(dto, product);

        // Create a map of existing variants for quick lookups.
        Map<Long, ProductVariant> existingVariantsMap = product.getVariants().stream()
                .collect(Collectors.toMap(ProductVariant::getProductVariantId, v -> v));

        List<ProductVariant> variantsToSave = new ArrayList<>();
        if (dto.getVariants() != null) {
            for (ProductVariantDto variantDto : dto.getVariants()) {
                ProductVariant variant;
                Long variantId = variantDto.getProductVariantId();

                if (variantId != null) {
                    // Update an existing variant.
                    variant = existingVariantsMap.get(variantId);
                    if (variant == null) {
                        // This case should ideally not happen if data is consistent.
                        throw new ResourceNotFoundException("ProductVariant not found with id: " + variantId);
                    }
                } else {
                    // Add a new variant.
                    variant = new ProductVariant();
                    variant.setProduct(product);
                }

                // Map properties from DTO to the variant entity.
                variant.setColor(variantDto.getColor());
                variant.setSize(variantDto.getSize());
                variant.setPrice(variantDto.getPrice());
                variant.setCurrency(variantDto.getCurrency());
                variant.setSku(dto.getProductCode() + "-" + variantDto.getColor() + "-" + variantDto.getSize());

                variantsToSave.add(variant);
            }
        }

        // Synchronize with the database: clear the old list and add the new one.
        product.getVariants().clear();
        product.getVariants().addAll(variantsToSave);

        Product savedProduct = productRepository.save(product);
        return convertEntityToDto(savedProduct);
    }

    /**
     * A private helper method to map fields from a {@link ProductDto} to a {@link Product} entity.
     * This encapsulates the mapping logic and ensures related entities like Category and Unit are fetched.
     *
     * @param dto     The source DTO.
     * @param product The target entity to be updated.
     */
    private void mapDtoToEntity(ProductDto dto, Product product) {
        // Fetch related entities. Throws ResourceNotFoundException if they don't exist.
        ProductCategory category = productCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + dto.getUnitId()));

        product.setProductCode(dto.getProductCode());
        product.setProductName(dto.getProductName());
        product.setProductCategory(category);
        product.setUnit(unit);
        product.setSeason(dto.getSeason());
        product.setStatus(dto.getStatus());
    }


    /**
     * Converts a Product entity to a fully detailed DTO, including its variants.
     *
     * @param product The source entity.
     * @return The fully populated {@link ProductDto}.
     */
    private ProductDto convertEntityToDto(Product product) {
        ProductDto dto = convertEntityToDtoSimple(product);
        dto.setSeason(product.getSeason());
        dto.setStatus(product.getStatus());

        if (product.getVariants() != null) {
            dto.setVariants(product.getVariants().stream()
                    .map(this::convertVariantEntityToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    /**
     * Converts a Product entity to a simplified DTO, intended for list views.
     * This version does not include the list of variants for better performance.
     *
     * @param product The source entity.
     * @return A simplified {@link ProductDto}.
     */
    private ProductDto convertEntityToDtoSimple(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setProductCode(product.getProductCode());
        dto.setProductName(product.getProductName());

        if (product.getProductCategory() != null) {
            dto.setCategoryId(product.getProductCategory().getCategoryId());
            dto.setCategoryName(product.getProductCategory().getCategoryName());
        }

        if (product.getUnit() != null) {
            dto.setUnitId(product.getUnit().getUnitId());
            dto.setUnitName(product.getUnit().getUnitName());
        }

        return dto;
    }

    /**
     * Converts a ProductVariant entity to its corresponding DTO.
     *
     * @param variant The source entity.
     * @return The {@link ProductVariantDto}.
     */
    private ProductVariantDto convertVariantEntityToDto(ProductVariant variant) {
        ProductVariantDto dto = new ProductVariantDto();
        dto.setProductVariantId(variant.getProductVariantId());
        dto.setColor(variant.getColor());
        dto.setSize(variant.getSize());
        dto.setSku(variant.getSku());
        dto.setPrice(variant.getPrice());
        dto.setCurrency(variant.getCurrency());
        return dto;
    }

    /**
     * {@inheritDoc}
     * <p><b>Use Case:</b> This method is triggered from the product list page when the user
     * types a keyword into the search box or selects a category to filter the list.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> search(String keyword, String categoryName) {
        // Ensure that empty strings are treated as null to match the JPQL query logic.
        String effectiveKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim().trim() : null;
        String effectiveCategory = (categoryName != null && !categoryName.trim().isEmpty()) ? categoryName.trim() : null;

        List<Product> products = productRepository.searchProducts(effectiveKeyword, effectiveCategory);

        AtomicInteger index = new AtomicInteger(1);
        return products.stream()
                .map(product -> {
                    ProductDto dto = convertEntityToDtoSimple(product);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Deletes product variants first to respect foreign key constraints, then deletes the product itself.
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        // It's better to iterate and handle each deletion individually to ensure existence.
        for (Long id : ids) {
            if (productRepository.existsById(id)) {
                // Important: Delete children before deleting the parent.
                productVariantRepository.deleteByProduct_ProductId(id);
                productRepository.deleteById(id);
            }
        }
    }

}