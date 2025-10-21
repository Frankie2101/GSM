package com.gsm.service;

import com.gsm.dto.TrimDto;
import com.gsm.dto.TrimVariantDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.*;
import com.gsm.repository.*;
import com.gsm.service.impl.TrimServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link TrimServiceImpl} class.
 * Validates business logic for managing trims, including the critical logic
 * for parsing comma-separated sizes into distinct variants.
 */
@ExtendWith(MockitoExtension.class)
class TrimServiceImplTest {

    //<editor-fold desc="Mocks and Service Under Test">
    @Mock
    private TrimRepository trimRepository;
    @Mock
    private UnitRepository unitRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private MaterialGroupRepository materialGroupRepository;
    @Mock
    private BOMTemplateDetailRepository bomTemplateDetailRepository;
    @Mock
    private OrderBOMDetailRepository orderBOMDetailRepository;

    @InjectMocks
    private TrimServiceImpl trimService;
    //</editor-fold>

    //<editor-fold desc="Test Data">
    private Trim sampleTrim;
    private Unit sampleUnit;
    private Supplier sampleSupplier;
    private MaterialGroup sampleMaterialGroup;
    //</editor-fold>

    @BeforeEach
    void setUp() {
        sampleUnit = new Unit();
        sampleUnit.setUnitId(10L);
        sampleSupplier = new Supplier();
        sampleSupplier.setSupplierId(20L);
        sampleMaterialGroup = new MaterialGroup();
        sampleMaterialGroup.setMaterialGroupId(30L);

        sampleTrim = new Trim();
        sampleTrim.setTrimId(1L);
        sampleTrim.setTrimCode("TR-001");
        sampleTrim.setUnit(sampleUnit);
        sampleTrim.setSupplier(sampleSupplier);
        sampleTrim.setMaterialGroup(sampleMaterialGroup);
        sampleTrim.setVariants(new ArrayList<>());
    }

    private void mockSaveDependencies() {
        when(unitRepository.findById(anyLong())).thenReturn(Optional.of(sampleUnit));
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(sampleSupplier));
        when(materialGroupRepository.findById(anyLong())).thenReturn(Optional.of(sampleMaterialGroup));
    }

    //<editor-fold desc="Tests for findById()">
    @Test
    void whenFindById_givenValidId_thenReturnTrimDto() {
        // Arrange
        when(trimRepository.findById(1L)).thenReturn(Optional.of(sampleTrim));
        when(bomTemplateDetailRepository.existsByTrim_TrimId(1L)).thenReturn(false);
        when(orderBOMDetailRepository.existsByTrim_TrimId(1L)).thenReturn(false);

        // Act
        TrimDto resultDto = trimService.findById(1L);

        // Assert
        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getTrimId());
        assertTrue(resultDto.isDeletable());
    }

    @Test
    void whenFindById_givenInvalidId_thenThrowResourceNotFoundException() {
        // Arrange
        when(trimRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trimService.findById(99L));
    }
    //</editor-fold>

    //<editor-fold desc="Tests for save()">
    @Test
    void whenSave_withNewTrim_thenCreatesSuccessfully() {
        // Arrange
        TrimDto newTrimDto = new TrimDto();
        newTrimDto.setTrimCode("TR-NEW");
        newTrimDto.setUnitId(10L);
        newTrimDto.setSupplierId(20L);
        newTrimDto.setMaterialGroupId(30L);
        newTrimDto.setVariants(new ArrayList<>());

        when(trimRepository.findByTrimCode("TR-NEW")).thenReturn(Optional.empty());
        mockSaveDependencies();
        when(trimRepository.save(any(Trim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        trimService.save(newTrimDto);

        // Assert
        ArgumentCaptor<Trim> trimCaptor = ArgumentCaptor.forClass(Trim.class);
        verify(trimRepository).save(trimCaptor.capture());
        assertEquals("TR-NEW", trimCaptor.getValue().getTrimCode());
    }

    @Test
    void whenSave_withNewTrimAndDuplicateCode_thenThrowDuplicateResourceException() {
        // Arrange
        TrimDto newTrimDto = new TrimDto();
        newTrimDto.setTrimCode("EXISTING-CODE");

        Trim existingTrim = new Trim();
        existingTrim.setTrimId(99L);
        existingTrim.setTrimCode("EXISTING-CODE");

        when(trimRepository.findByTrimCode("EXISTING-CODE")).thenReturn(Optional.of(existingTrim));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> trimService.save(newTrimDto));
    }

    /**
     * This test specifically validates the core business logic of splitting a comma-separated
     * size string into multiple, distinct TrimVariant entities.
     */
    @Test
    void whenSave_withNewTrimAndCommaSeparatedSizes_thenCreatesMultipleVariants() {
        // Arrange
        TrimDto newTrimDto = new TrimDto();
        newTrimDto.setTrimCode("TR-MULTI-SIZE");
        newTrimDto.setUnitId(10L);
        newTrimDto.setSupplierId(20L);
        newTrimDto.setMaterialGroupId(30L);

        TrimVariantDto variantWithMultipleSizes = new TrimVariantDto();
        variantWithMultipleSizes.setColorCode("BLK");
        // --- Key test data: comma-separated string ---
        variantWithMultipleSizes.setSizeCode("120mm, 150mm, 180mm");
        newTrimDto.setVariants(Arrays.asList(variantWithMultipleSizes));

        when(trimRepository.findByTrimCode("TR-MULTI-SIZE")).thenReturn(Optional.empty());
        mockSaveDependencies();
        when(trimRepository.save(any(Trim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        trimService.save(newTrimDto);

        // Assert
        ArgumentCaptor<Trim> trimCaptor = ArgumentCaptor.forClass(Trim.class);
        verify(trimRepository).save(trimCaptor.capture());
        Trim savedTrim = trimCaptor.getValue();

        // The service should have created 3 distinct variants from the single DTO.
        assertEquals(3, savedTrim.getVariants().size());

        // Verify the sizes were parsed correctly.
        assertTrue(savedTrim.getVariants().stream().anyMatch(v -> "120mm".equals(v.getSizeCode())));
        assertTrue(savedTrim.getVariants().stream().anyMatch(v -> "150mm".equals(v.getSizeCode())));
        assertTrue(savedTrim.getVariants().stream().anyMatch(v -> "180mm".equals(v.getSizeCode())));
    }

    @Test
    void whenSave_withExistingTrim_thenUpdatesAndAddsVariants() {
        // Arrange: An existing trim with one variant
        TrimVariant existingVariant = new TrimVariant();
        existingVariant.setTrimVariantId(101L);
        existingVariant.setColorCode("BLK");
        existingVariant.setSizeCode("100mm");
        sampleTrim.addVariant(existingVariant); // Use helper to set bidirectional link

        // Arrange: DTO to update the trim. Will update the existing variant
        // and add two new variants via a comma-separated size string.
        TrimDto updateDto = new TrimDto();
        updateDto.setTrimId(1L);
        updateDto.setTrimCode("TR-001");
        updateDto.setUnitId(10L);
        updateDto.setSupplierId(20L);
        updateDto.setMaterialGroupId(30L);

        // DTO to update the existing variant (ID 101L)
        TrimVariantDto updatedVariantDto = new TrimVariantDto();
        updatedVariantDto.setTrimVariantId(101L);
        updatedVariantDto.setColorCode("BLK");
        updatedVariantDto.setSizeCode("100mm");
        updatedVariantDto.setNetPrice(1.25); // Price is updated

        // DTO to add two new variants
        TrimVariantDto newVariantsDto = new TrimVariantDto();
        newVariantsDto.setColorCode("WHT");
        newVariantsDto.setSizeCode("120mm, 150mm");

        updateDto.setVariants(Arrays.asList(updatedVariantDto, newVariantsDto));

        // Arrange: Mock repository calls
        when(trimRepository.findById(1L)).thenReturn(Optional.of(sampleTrim));
        when(trimRepository.findByTrimCode("TR-001")).thenReturn(Optional.of(sampleTrim));
        mockSaveDependencies();
        when(orderBOMDetailRepository.existsByTrimIdAndColorCodeAndSizeCode(any(), any(), any())).thenReturn(false);
        when(trimRepository.save(any(Trim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        trimService.save(updateDto);

        // Assert
        ArgumentCaptor<Trim> trimCaptor = ArgumentCaptor.forClass(Trim.class);
        verify(trimRepository).save(trimCaptor.capture());
        Trim savedTrim = trimCaptor.getValue();

        // Should have 1 updated + 2 new variants = 3 total.
        assertEquals(3, savedTrim.getVariants().size());

        // Check if the old variant was updated
        Optional<TrimVariant> updatedOptional = savedTrim.getVariants().stream()
                .filter(v -> v.getTrimVariantId() != null && v.getTrimVariantId().equals(101L))
                .findFirst();
        assertTrue(updatedOptional.isPresent());
        assertEquals(1.25, updatedOptional.get().getNetPrice());

        // Check if the two new variants were added
        assertTrue(savedTrim.getVariants().stream().anyMatch(v -> "WHT".equals(v.getColorCode()) && "120mm".equals(v.getSizeCode())));
        assertTrue(savedTrim.getVariants().stream().anyMatch(v -> "WHT".equals(v.getColorCode()) && "150mm".equals(v.getSizeCode())));
    }
    //</editor-fold>
}