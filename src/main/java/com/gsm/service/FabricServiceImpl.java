package com.gsm.service;

import com.gsm.dto.FabricColorDto;
import com.gsm.dto.FabricDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.*;
import com.gsm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import com.gsm.repository.MaterialGroupRepository;
import com.gsm.repository.BOMTemplateDetailRepository;
import com.gsm.repository.OrderBOMDetailRepository;
import java.util.stream.Stream;

/**
 * The concrete implementation of the {@link FabricService} interface.
 * This class orchestrates all business logic for the Fabric feature,
 * including validation, data mapping, and persistence.
 */
@Service
public class FabricServiceImpl implements FabricService {

    private final FabricRepository fabricRepository;
    private final FabricColorRepository fabricColorRepository;
    private final UnitRepository unitRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialGroupRepository materialGroupRepository;
    private final BOMTemplateDetailRepository bomTemplateDetailRepository;
    private final OrderBOMDetailRepository orderBOMDetailRepository;

    @Autowired
    public FabricServiceImpl(FabricRepository fabricRepository, FabricColorRepository fabricColorRepository, UnitRepository unitRepository, SupplierRepository supplierRepository, MaterialGroupRepository materialGroupRepository, BOMTemplateDetailRepository bomTemplateDetailRepository, OrderBOMDetailRepository orderBOMDetailRepository) {
        this.fabricRepository = fabricRepository;
        this.fabricColorRepository = fabricColorRepository;
        this.unitRepository = unitRepository;
        this.supplierRepository = supplierRepository;
        this.materialGroupRepository = materialGroupRepository;
        this.bomTemplateDetailRepository = bomTemplateDetailRepository;
        this.orderBOMDetailRepository = orderBOMDetailRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<FabricDto> findAll() {
        Set<Long> bomIds = bomTemplateDetailRepository.findDistinctFabricIdsInUse();
        Set<Long> orderBomIds = orderBOMDetailRepository.findDistinctFabricIdsInUse();
        Set<Long> nonDeletableIds = Stream.concat(bomIds.stream(), orderBomIds.stream()).collect(Collectors.toSet());

        AtomicInteger index = new AtomicInteger(1);
        return fabricRepository.findAll().stream()
                .map(fabric -> {
                    FabricDto dto = convertEntityToDtoSimple(fabric);
                    dto.setSequenceNumber((long) index.getAndIncrement());

                    if (nonDeletableIds.contains(fabric.getFabricId())) {
                        dto.setDeletable(false);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public FabricDto findById(Long id) {
        Fabric fabric = fabricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fabric not found with id: " + id));
        FabricDto dto = convertEntityToDto(fabric);

        boolean isInBOM = bomTemplateDetailRepository.existsByFabric_FabricId(id) ||
                orderBOMDetailRepository.existsByFabric_FabricId(id);
        if (isInBOM) {
            dto.setDeletable(false);
        }
        return dto;
    }

    /**
     * {@inheritDoc}
     * <p><b>Use Case:</b> Executed when the user saves the fabric form.
     * This implementation ensures data integrity by validating uniqueness and
     * correctly synchronizing the nested list of fabric colors.
     */
    @Override
    @Transactional
    public FabricDto save(FabricDto dto) {
        // 1. Validate for duplicate fabric code before proceeding.
        fabricRepository.findByFabricCode(dto.getFabricCode()).ifPresent(existing -> {
            if (dto.getFabricId() == null || !existing.getFabricId().equals(dto.getFabricId())) {
                throw new DuplicateResourceException("Fabric Code '" + dto.getFabricCode() + "' already exists.");
            }
        });

        // 2. Fetch the existing entity for an update, or create a new one.
        Fabric fabric = (dto.getFabricId() != null)
                ? fabricRepository.findById(dto.getFabricId())
                .orElseThrow(() -> new ResourceNotFoundException("Fabric not found with id: " + dto.getFabricId()))
                : new Fabric();

        // 3. Map simple properties from DTO to the entity.
        mapDtoToEntity(dto, fabric);

        // 4. Synchronize the collection of child entities (FabricColors).
        Map<Long, FabricColor> existingColorsMap = fabric.getFabricColors().stream()
                .collect(Collectors.toMap(FabricColor::getFabricColorId, color -> color));

        List<FabricColor> updatedColors = new ArrayList<>();

        if (dto.getFabricColors() != null) {
            for (FabricColorDto colorDto : dto.getFabricColors()) {
                FabricColor color;
                Long colorId = colorDto.getFabricColorId();

                if (colorId != null) {
                    color = existingColorsMap.get(colorId);
                    if (color == null) {
                        throw new ResourceNotFoundException("FabricColor not found with id: " + colorId);
                    }
                }
                else {
                    color = new FabricColor();
                    color.setFabric(fabric);
                }

                // Update properties for both new and existing colors.
                color.setColor(colorDto.getColor());
                color.setColorName(colorDto.getColorName());
                color.setWidth(colorDto.getWidth());
                color.setNetPrice(colorDto.getNetPrice());
                color.setTaxPercent(colorDto.getTaxPercent());
                updatedColors.add(color);
            }
        }

        // By clearing and re-adding, use`orphanRemoval=true` in the Fabric entity
        // to automatically delete any colors that were removed from the UI.
        fabric.getFabricColors().clear();
        fabric.getFabricColors().addAll(updatedColors);

        // 5. Save the parent entity. JPA cascades all changes to the children.
        Fabric savedFabric = fabricRepository.save(fabric);

        return convertEntityToDto(savedFabric);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        List<String> undeletableFabrics = new ArrayList<>();
        for (Long id : ids) {
            boolean isInBOM = bomTemplateDetailRepository.existsByFabric_FabricId(id) ||
                    orderBOMDetailRepository.existsByFabric_FabricId(id);
            if (isInBOM) {
                fabricRepository.findById(id).ifPresent(fab -> undeletableFabrics.add(fab.getFabricCode()));
            }
        }

        if (!undeletableFabrics.isEmpty()) {
            throw new IllegalStateException("Cannot delete fabrics: " + String.join(", ", undeletableFabrics) + ". It is existing in BOM.");
        }

        for (Long id : ids) {
            if (fabricRepository.existsById(id)) {
                fabricColorRepository.deleteByFabric_FabricId(id);
                fabricRepository.deleteById(id);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<FabricDto> search(String keyword) {
        Set<Long> bomIds = bomTemplateDetailRepository.findDistinctFabricIdsInUse();
        Set<Long> orderBomIds = orderBOMDetailRepository.findDistinctFabricIdsInUse();
        Set<Long> nonDeletableIds = Stream.concat(bomIds.stream(), orderBomIds.stream()).collect(Collectors.toSet());

        String effectiveKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Fabric> fabrics = fabricRepository.searchFabrics(effectiveKeyword);
        AtomicInteger index = new AtomicInteger(1);

        return fabrics.stream()
                .map(fabric -> {
                    FabricDto dto = convertEntityToDtoSimple(fabric);
                    dto.setSequenceNumber((long) index.getAndIncrement());

                    if (nonDeletableIds.contains(fabric.getFabricId())) {
                        dto.setDeletable(false);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---

    /**
     * Private helper to map data from a DTO to an existing Fabric entity.
     * @param dto The source DTO.
     * @param fabric The target entity.
     */
    private void mapDtoToEntity(FabricDto dto, Fabric fabric) {
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + dto.getUnitId()));
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + dto.getSupplierId()));
        MaterialGroup group = materialGroupRepository.findById(dto.getMaterialGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Material Group not found with ID: " + dto.getMaterialGroupId()));

        fabric.setActiveFlag(dto.isActiveFlag());
        fabric.setFabricCode(dto.getFabricCode());
        fabric.setFabricName(dto.getFabricName());
        fabric.setFabricType(dto.getFabricType());
        fabric.setConstruction(dto.getConstruction());
        fabric.setYarnCount(dto.getYarnCount());
        fabric.setFinishing(dto.getFinishing());
        fabric.setFabricContent(dto.getFabricContent());
        fabric.setUnit(unit);
        fabric.setSupplier(supplier);
        fabric.setMaterialGroup(group);
    }

    /**
     * Private helper to convert a Fabric entity to a full DTO, including details.
     * @param fabric The source entity.
     * @return A detailed FabricDto.
     */
    private FabricDto convertEntityToDto(Fabric fabric) {
        FabricDto dto = new FabricDto();
        dto.setFabricId(fabric.getFabricId());
        dto.setFabricCode(fabric.getFabricCode());
        dto.setFabricName(fabric.getFabricName());
        dto.setFabricType(fabric.getFabricType());
        dto.setConstruction(fabric.getConstruction());
        dto.setYarnCount(fabric.getYarnCount());
        dto.setFinishing(fabric.getFinishing());
        dto.setFabricContent(fabric.getFabricContent());
        dto.setActiveFlag(fabric.isActiveFlag());
        dto.setMaterialGroupId(fabric.getMaterialGroup().getMaterialGroupId());

        if (fabric.getUnit() != null) {
            dto.setUnitId(fabric.getUnit().getUnitId());
            dto.setUnitName(fabric.getUnit().getUnitName());
        }

        if (fabric.getSupplier() != null) {
            dto.setSupplierId(fabric.getSupplier().getSupplierId());
            dto.setSupplierName(fabric.getSupplier().getSupplierName());
        }

        if (fabric.getMaterialGroup() != null) {
            dto.setMaterialGroupId(fabric.getMaterialGroup().getMaterialGroupId());
            dto.setMaterialGroupName(fabric.getMaterialGroup().getMaterialGroupName());
        }

        if (fabric.getFabricColors() != null) {
            dto.setFabricColors(fabric.getFabricColors().stream()
                    .map(this::convertColorEntityToDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setFabricColors(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Private helper to convert a Fabric entity to a simple DTO for list views.
     * @param fabric The source entity.
     * @return A simplified FabricDto.
     */
    private FabricDto convertEntityToDtoSimple(Fabric fabric) {
        FabricDto dto = new FabricDto();
        dto.setFabricId(fabric.getFabricId());
        dto.setFabricCode(fabric.getFabricCode());
        dto.setFabricName(fabric.getFabricName());
        dto.setFabricType(fabric.getFabricType());

        if (fabric.getUnit() != null) {
            dto.setUnitId(fabric.getUnit().getUnitId());
            dto.setUnitName(fabric.getUnit().getUnitName());
        }

        if (fabric.getSupplier() != null) {
            dto.setSupplierId(fabric.getSupplier().getSupplierId());
            dto.setSupplierName(fabric.getSupplier().getSupplierName());
        }

        if (fabric.getMaterialGroup() != null) {
            dto.setMaterialGroupId(fabric.getMaterialGroup().getMaterialGroupId());
            dto.setMaterialGroupName(fabric.getMaterialGroup().getMaterialGroupName());
        }

        return dto;
    }

    /**
     * Private helper to convert a FabricColor entity to its DTO representation.
     * @param color The source entity.
     * @return A FabricColorDto.
     */
    private FabricColorDto convertColorEntityToDto(FabricColor color) {
        FabricColorDto dto = new FabricColorDto();
        dto.setFabricColorId(color.getFabricColorId());
        dto.setColor(color.getColor());
        dto.setColorName(color.getColorName());
        dto.setWidth(color.getWidth());
        dto.setNetPrice(color.getNetPrice());
        dto.setTaxPercent(color.getTaxPercent());

        if (color.getFabricColorId() != null) {
            Long fabricId = color.getFabric().getFabricId();
            String colorCode = color.getColor();

            if (orderBOMDetailRepository.existsByFabricIdAndColorCode(fabricId, colorCode)) {
                dto.setDeletable(false);
            }
        }
        return dto;
    }
}
