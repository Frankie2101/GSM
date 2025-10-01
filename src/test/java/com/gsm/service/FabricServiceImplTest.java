package com.gsm.service;

import com.gsm.dto.FabricColorDto;
import com.gsm.dto.FabricDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.*;
import com.gsm.repository.*;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link FabricServiceImpl} class.
 * This class validates the business logic of the fabric service in isolation
 * from the database, focusing on data mapping, validation, and synchronization.
 */
@ExtendWith(MockitoExtension.class)
class FabricServiceImplTest {

    //<editor-fold desc="Mocks and Service Under Test">
    @Mock
    private FabricRepository fabricRepository;
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
    private FabricServiceImpl fabricService;
    //</editor-fold>

    //<editor-fold desc="Test Data">
    private Fabric sampleFabric;
    private Unit sampleUnit;
    private Supplier sampleSupplier;
    private MaterialGroup sampleMaterialGroup;
    //</editor-fold>

    /**
     * Sets up common test data before each test execution.
     */
    @BeforeEach
    void setUp() {
        sampleUnit = new Unit();
        sampleUnit.setUnitId(10L);
        sampleSupplier = new Supplier();
        sampleSupplier.setSupplierId(20L);
        sampleMaterialGroup = new MaterialGroup();
        sampleMaterialGroup.setMaterialGroupId(30L);

        sampleFabric = new Fabric();
        sampleFabric.setFabricId(1L);
        sampleFabric.setFabricCode("FAB-001");
        sampleFabric.setUnit(sampleUnit);
        sampleFabric.setSupplier(sampleSupplier);
        sampleFabric.setMaterialGroup(sampleMaterialGroup);
        sampleFabric.setFabricColors(new ArrayList<>());
    }

    /**
     * Helper method to configure common mock behaviors for repositories
     * that are used across multiple 'save' tests.
     */
    private void mockSaveDependencies() {
        when(unitRepository.findById(anyLong())).thenReturn(Optional.of(sampleUnit));
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(sampleSupplier));
        when(materialGroupRepository.findById(anyLong())).thenReturn(Optional.of(sampleMaterialGroup));
    }

    //<editor-fold desc="Tests for findById()">
    /**
     * Tests findById for a valid ID where the fabric is not in use (deletable).
     */
    @Test
    void whenFindById_givenValidId_thenReturnFabricDto() {
        // Arrange
        when(fabricRepository.findById(1L)).thenReturn(Optional.of(sampleFabric));
        when(bomTemplateDetailRepository.existsByFabric_FabricId(1L)).thenReturn(false);
        when(orderBOMDetailRepository.existsByFabric_FabricId(1L)).thenReturn(false);

        // Act
        FabricDto resultDto = fabricService.findById(1L);

        // Assert
        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getFabricId());
        assertTrue(resultDto.isDeletable());
    }

    /**
     * Tests findById for a valid ID where the fabric is in use (not deletable).
     */
    @Test
    void whenFindById_givenInUseFabric_thenSetDeletableToFalse() {
        // Arrange
        when(fabricRepository.findById(1L)).thenReturn(Optional.of(sampleFabric));
        when(bomTemplateDetailRepository.existsByFabric_FabricId(1L)).thenReturn(true);

        // Act
        FabricDto resultDto = fabricService.findById(1L);

        // Assert
        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getFabricId());
        assertFalse(resultDto.isDeletable());
    }

    /**
     * Tests findById for an invalid/non-existent ID.
     */
    @Test
    void whenFindById_givenInvalidId_thenThrowResourceNotFoundException() {
        // Arrange
        when(fabricRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> fabricService.findById(99L));
    }
    //</editor-fold>

    //<editor-fold desc="Tests for save()">
    /**
     * Tests creating a new fabric successfully.
     */
    @Test
    void whenSave_withNewFabric_thenCreatesSuccessfully() {
        // Arrange
        FabricDto newFabricDto = new FabricDto(); // ID is null
        newFabricDto.setFabricCode("FAB-002");
        newFabricDto.setUnitId(10L);
        newFabricDto.setSupplierId(20L);
        newFabricDto.setMaterialGroupId(30L);

        when(fabricRepository.findByFabricCode("FAB-002")).thenReturn(Optional.empty()); // No duplicate
        mockSaveDependencies();
        when(fabricRepository.save(any(Fabric.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        fabricService.save(newFabricDto);

        // Assert
        ArgumentCaptor<Fabric> fabricCaptor = ArgumentCaptor.forClass(Fabric.class);
        verify(fabricRepository).save(fabricCaptor.capture());
        assertEquals("FAB-002", fabricCaptor.getValue().getFabricCode());
    }

    /**
     * Tests creating a new fabric with a code that already exists.
     */
    @Test
    void whenSave_withNewFabricAndDuplicateCode_thenThrowDuplicateResourceException() {
        // Arrange
        FabricDto newFabricDto = new FabricDto();
        newFabricDto.setFabricCode("EXISTING-CODE");

        Fabric existingFabric = new Fabric();
        existingFabric.setFabricId(99L); // Crucially, it has a different ID
        existingFabric.setFabricCode("EXISTING-CODE");

        when(fabricRepository.findByFabricCode("EXISTING-CODE")).thenReturn(Optional.of(existingFabric));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> fabricService.save(newFabricDto));
    }

    /**
     * Tests updating an existing fabric, including adding, updating, and removing colors.
     * This is the most complex scenario.
     */
    @Test
    void whenSave_withExistingFabric_thenUpdatesAndAddsAndRemovesColors() {
        // Arrange: Create an existing fabric with two colors: RED and BLUE.
        FabricColor existingRed = new FabricColor();
        existingRed.setFabricColorId(101L);
        existingRed.setColor("RED");

        FabricColor existingBlue = new FabricColor();
        existingBlue.setFabricColorId(102L);
        existingBlue.setColor("BLUE");
        existingBlue.setColorName("Sky Blue");

        // === FIX IS HERE ===
        // Use the helper method to establish the bidirectional link correctly.
        sampleFabric.addColor(existingRed);
        sampleFabric.addColor(existingBlue);

        // Arrange: Create a DTO to simulate UI changes.
        // We will update BLUE, remove RED, and add GREEN.
        FabricDto updateDto = new FabricDto();
        updateDto.setFabricId(1L);
        updateDto.setFabricCode("FAB-001");
        updateDto.setUnitId(10L);
        updateDto.setSupplierId(20L);
        updateDto.setMaterialGroupId(30L);

        // DTO for updating BLUE color
        FabricColorDto updatedBlueDto = new FabricColorDto();
        updatedBlueDto.setFabricColorId(102L);
        updatedBlueDto.setColor("BLUE");
        updatedBlueDto.setColorName("Navy Blue"); // Name changed

        // DTO for adding new GREEN color
        FabricColorDto newGreenDto = new FabricColorDto();
        newGreenDto.setColor("GREEN"); // ID is null

        updateDto.setFabricColors(Arrays.asList(updatedBlueDto, newGreenDto));

        // Arrange: Mock repository behaviors.
        when(fabricRepository.findById(1L)).thenReturn(Optional.of(sampleFabric));
        when(fabricRepository.findByFabricCode("FAB-001")).thenReturn(Optional.of(sampleFabric)); // Pass duplicate check
        mockSaveDependencies();
        // Mock the logic for checking deletable status in the return DTO
        when(orderBOMDetailRepository.existsByFabricIdAndColorCode(anyLong(), anyString())).thenReturn(false);
        when(fabricRepository.save(any(Fabric.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        fabricService.save(updateDto);

        // Assert
        ArgumentCaptor<Fabric> fabricCaptor = ArgumentCaptor.forClass(Fabric.class);
        verify(fabricRepository).save(fabricCaptor.capture());
        Fabric savedFabric = fabricCaptor.getValue();

        // 1. Final list should have 2 colors (BLUE and GREEN), RED should be removed.
        assertEquals(2, savedFabric.getFabricColors().size());

        // 2. Verify the BLUE color was updated correctly.
        Optional<FabricColor> blueOptional = savedFabric.getFabricColors().stream()
                .filter(c -> "BLUE".equals(c.getColor())).findFirst();
        assertTrue(blueOptional.isPresent());
        assertEquals("Navy Blue", blueOptional.get().getColorName());

        // 3. Verify the GREEN color was added.
        boolean greenExists = savedFabric.getFabricColors().stream()
                .anyMatch(c -> "GREEN".equals(c.getColor()));
        assertTrue(greenExists);

        // 4. Verify the RED color was removed.
        boolean redExists = savedFabric.getFabricColors().stream()
                .anyMatch(c -> "RED".equals(c.getColor()));
        assertFalse(redExists);
    }
    //</editor-fold>
}