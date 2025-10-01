package com.gsm.service;

import com.gsm.dto.ProductDto;
import com.gsm.dto.ProductVariantDto;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.Product;
import com.gsm.model.ProductCategory;
import com.gsm.model.ProductVariant;
import com.gsm.model.Unit;
import com.gsm.repository.ProductCategoryRepository;
import com.gsm.repository.ProductRepository;
import com.gsm.repository.ProductVariantRepository;
import com.gsm.repository.SaleOrderDetailRepository;
import com.gsm.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link ProductServiceImpl} class.
 * This class validates the business logic of the product service in isolation
 * from the database and other external dependencies.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    //<editor-fold desc="Mocks and Service Under Test">
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @Mock
    private UnitRepository unitRepository;
    @Mock
    private SaleOrderDetailRepository saleOrderDetailRepository;

    @InjectMocks
    private ProductServiceImpl productService;
    //</editor-fold>

    //<editor-fold desc="Test Data">
    private Product sampleProduct;
    private ProductCategory sampleCategory;
    private Unit sampleUnit;
    //</editor-fold>

    /**
     * Sets up common test data before each test execution.
     * This ensures that each test runs with a fresh, consistent set of objects.
     */
    @BeforeEach
    void setUp() {
        sampleCategory = new ProductCategory();
        sampleCategory.setCategoryId(10L);
        sampleUnit = new Unit();
        sampleUnit.setUnitId(20L);
        sampleProduct = new Product();
        sampleProduct.setProductId(1L);
        sampleProduct.setProductCategory(sampleCategory);
        sampleProduct.setUnit(sampleUnit);
        sampleProduct.setVariants(new ArrayList<>());
    }

    /**
     * Helper method to configure common mock behaviors for repositories
     * that are used across multiple 'save' tests. This reduces code duplication.
     */
    private void mockHelperRepositories() {
        when(productCategoryRepository.findById(anyLong())).thenReturn(Optional.of(sampleCategory));
        when(unitRepository.findById(anyLong())).thenReturn(Optional.of(sampleUnit));
        when(saleOrderDetailRepository.existsByProductVariant_ProductVariantId(any())).thenReturn(false);
    }

    /**
     * Tests {@code findById} for the scenario where a valid ID is provided and the product exists.
     * It verifies that a non-null DTO with the correct ID is returned.
     */
    @Test
    void whenFindById_givenValidId_thenReturnProductDto() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        // Act
        ProductDto resultDto = productService.findById(1L);

        // Assert
        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getProductId());
    }

    /**
     * Tests {@code findById} for the scenario where an invalid ID is provided.
     * It verifies that a {@link ResourceNotFoundException} is thrown.
     */
    @Test
    void whenFindById_givenInvalidId_thenThrowResourceNotFoundException() {
        // Arrange
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.findById(99L));
    }

    /**
     * Tests the {@code save} method for creating a new product with a single variant.
     * It verifies that the repository's save method is called.
     */
    @Test
    void whenSave_withNewProductAndSingleVariant_thenCreatesSuccessfully() {
        // Arrange
        ProductDto newProductDto = new ProductDto();
        newProductDto.setProductCode("P002");
        newProductDto.setCategoryId(10L);
        newProductDto.setUnitId(20L);
        ProductVariantDto variantDto = new ProductVariantDto();
        variantDto.setColor("Red");
        variantDto.setSize("M");
        newProductDto.setVariants(Arrays.asList(variantDto));

        mockHelperRepositories();
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        productService.save(newProductDto);

        // Assert
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
    }

    /**
     * Tests the {@code save} method's ability to parse a comma-separated size string
     * from a single DTO into multiple, distinct {@link ProductVariant} entities.
     */
    @Test
    void whenSave_withNewProductAndCommaSeparatedSizes_thenCreatesMultipleVariants() {
        // Arrange
        ProductDto newProductDto = new ProductDto();
        newProductDto.setProductCode("P003");
        newProductDto.setCategoryId(10L);
        newProductDto.setUnitId(20L);
        ProductVariantDto variantDto = new ProductVariantDto();
        variantDto.setColor("Blue");
        variantDto.setSize("S, M, L");
        newProductDto.setVariants(Arrays.asList(variantDto));

        mockHelperRepositories();
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        productService.save(newProductDto);

        // Assert
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();
        assertEquals(3, capturedProduct.getVariants().size());
    }

    /**
     * Tests the complex update scenario for the {@code save} method.
     * It verifies that the service can simultaneously update an existing variant
     * and add a new variant to a product in a single operation.
     */
    @Test
    void whenSave_withExistingProduct_thenUpdatesAndAddsVariants() {
        // Arrange: Create an existing product with one variant.
        ProductVariant existingVariant = new ProductVariant();
        existingVariant.setProductVariantId(101L);
        existingVariant.setProduct(sampleProduct); // Establish the bidirectional link.
        existingVariant.setSize("M");
        sampleProduct.getVariants().add(existingVariant);

        // Arrange: Create a DTO that updates the existing variant and adds a new one.
        ProductDto updateDto = new ProductDto();
        updateDto.setProductId(1L);
        updateDto.setProductName("New Updated Name");
        updateDto.setCategoryId(10L);
        updateDto.setUnitId(20L);

        ProductVariantDto updatedVariantDto = new ProductVariantDto();
        updatedVariantDto.setProductVariantId(101L);
        updatedVariantDto.setSize("M"); // Provide size to prevent being filtered out by the service logic.

        ProductVariantDto newVariantDto = new ProductVariantDto();
        newVariantDto.setSize("L");
        updateDto.setVariants(Arrays.asList(updatedVariantDto, newVariantDto));

        // Arrange: Mock repository behaviors.
        mockHelperRepositories();
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        productService.save(updateDto);

        // Assert: Verify the final product state has the correct number of variants.
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();
        assertEquals(2, capturedProduct.getVariants().size());
    }
}