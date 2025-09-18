package com.gsm.service;

import com.gsm.dto.FabricColorDto;
import com.gsm.dto.FabricDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.Fabric;
import com.gsm.model.FabricColor;
import com.gsm.model.Supplier;
import com.gsm.model.Unit;
import com.gsm.repository.FabricColorRepository;
import com.gsm.repository.FabricRepository;
import com.gsm.repository.SupplierRepository;
import com.gsm.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    @Autowired
    public FabricServiceImpl(FabricRepository fabricRepository, FabricColorRepository fabricColorRepository, UnitRepository unitRepository, SupplierRepository supplierRepository) {
        this.fabricRepository = fabricRepository;
        this.fabricColorRepository = fabricColorRepository;
        this.unitRepository = unitRepository;
        this.supplierRepository = supplierRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<FabricDto> findAll() {
        AtomicInteger index = new AtomicInteger(1);
        return fabricRepository.findAll().stream()
                .map(fabric -> {
                    FabricDto dto = convertEntityToDtoSimple(fabric);
                    dto.setSequenceNumber((long) index.getAndIncrement());
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
        return convertEntityToDto(fabric);
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
                        // Xử lý trường hợp ID không hợp lệ nếu cần
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
        String effectiveKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Fabric> fabrics = fabricRepository.searchFabrics(effectiveKeyword);
        AtomicInteger index = new AtomicInteger(1);
        return fabrics.stream()
                .map(fabric -> {
                    FabricDto dto = convertEntityToDtoSimple(fabric);
                    dto.setSequenceNumber((long) index.getAndIncrement());
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

        fabric.setActiveFlag(dto.isActiveFlag());
        fabric.setFabricCode(dto.getFabricCode());
        fabric.setFabricName(dto.getFabricName());
        fabric.setFabricType(dto.getFabricType());

        // GÁN DỮ LIỆU TỪ CÁC TRƯỜNG MỚI
        fabric.setConstruction(dto.getConstruction());
        fabric.setYarnCount(dto.getYarnCount());
        fabric.setFinishing(dto.getFinishing());
        fabric.setFabricContent(dto.getFabricContent());

        fabric.setUnit(unit);
        fabric.setSupplier(supplier);
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

        if (fabric.getUnit() != null) {
            dto.setUnitId(fabric.getUnit().getUnitId());
            dto.setUnitName(fabric.getUnit().getUnitName());
        }

        if (fabric.getSupplier() != null) {
            dto.setSupplierId(fabric.getSupplier().getSupplierId());
            dto.setSupplierName(fabric.getSupplier().getSupplierName());
        }

        if (fabric.getFabricColors() != null) {
            dto.setFabricColors(fabric.getFabricColors().stream()
                    .map(this::convertColorEntityToDto) // Bây giờ sẽ gọi đúng phương thức bên dưới
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
        return dto;
    }
}
