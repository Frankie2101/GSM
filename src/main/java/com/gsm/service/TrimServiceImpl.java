package com.gsm.service;

import com.gsm.dto.TrimDto;
import com.gsm.dto.TrimVariantDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.Supplier;
import com.gsm.model.Trim;
import com.gsm.model.TrimVariant;
import com.gsm.model.Unit;
import com.gsm.repository.SupplierRepository;
import com.gsm.repository.TrimRepository;
import com.gsm.repository.TrimVariantRepository;
import com.gsm.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The concrete implementation of the {@link TrimService} interface.
 * This class orchestrates all business logic for the Trim feature.
 */
@Service
public class TrimServiceImpl implements TrimService {

    private final TrimRepository trimRepository;
    private final TrimVariantRepository trimVariantRepository;
    private final UnitRepository unitRepository;
    private final SupplierRepository supplierRepository;

    @Autowired
    public TrimServiceImpl(TrimRepository trimRepository, TrimVariantRepository trimVariantRepository, UnitRepository unitRepository, SupplierRepository supplierRepository) {
        this.trimRepository = trimRepository;
        this.trimVariantRepository = trimVariantRepository;
        this.unitRepository = unitRepository;
        this.supplierRepository = supplierRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<TrimDto> findAll() {
        AtomicInteger index = new AtomicInteger(1);
        return trimRepository.findAll().stream()
                .map(trim -> {
                    TrimDto dto = convertEntityToDtoSimple(trim);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public TrimDto findById(Long id) {
        Trim trim = trimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trim not found with id: " + id));
        return convertEntityToDto(trim);
    }

    /**
     * {@inheritDoc}
     * This method now uses an intelligent synchronization strategy for variants,
     * consistent with other services. It correctly handles updates, additions, and deletions.
     */
    @Override
    @Transactional
    public TrimDto save(TrimDto dto) {
        // 1. Validate for duplicate trim code.
        trimRepository.findByTrimCode(dto.getTrimCode()).ifPresent(existing -> {
            if (dto.getTrimId() == null || !existing.getTrimId().equals(dto.getTrimId())) {
                throw new DuplicateResourceException("Trim Code '" + dto.getTrimCode() + "' already exists.");
            }
        });

        // 2. Fetch the existing entity for an update, or create a new one.
        Trim trim = (dto.getTrimId() != null)
                ? trimRepository.findById(dto.getTrimId())
                .orElseThrow(() -> new ResourceNotFoundException("Trim not found with id: " + dto.getTrimId()))
                : new Trim();

        // 3. Map simple properties from DTO to the entity.
        mapDtoToEntity(dto, trim);

        // Pre-process variants to handle comma-separated size codes
        List<TrimVariantDto> processedVariants = new ArrayList<>();
        if (dto.getVariants() != null) {
            dto.getVariants().stream()
                    .filter(variantDto -> variantDto.getSizeCode() != null && !variantDto.getSizeCode().trim().isEmpty())
                    .forEach(variantDto -> {
                        String[] sizeCodes = variantDto.getSizeCode().split("\\s*,\\s*");
                        boolean isFirstSize = true;
                        for (String size : sizeCodes) {
                            if (size.trim().isEmpty()) continue;
                            TrimVariantDto newDto = new TrimVariantDto();
                            newDto.setColorCode(variantDto.getColorCode());
                            newDto.setColorName(variantDto.getColorName());
                            newDto.setNetPrice(variantDto.getNetPrice());
                            newDto.setTaxRate(variantDto.getTaxRate());
                            newDto.setSizeCode(size.trim());
                            if (isFirstSize) {
                                newDto.setTrimVariantId(variantDto.getTrimVariantId());
                                isFirstSize = false;
                            } else {
                                newDto.setTrimVariantId(null);
                            }
                            processedVariants.add(newDto);
                        }
                    });
        }

        // 4.VARIANT SYNCHRONIZATION LOGIC ---

        // Create a map of existing variants for quick lookups.
        Map<Long, TrimVariant> existingVariantsMap = trim.getVariants().stream()
                .collect(Collectors.toMap(TrimVariant::getTrimVariantId, v -> v));

        List<TrimVariant> variantsToSave = new ArrayList<>();
        for (TrimVariantDto variantDto : processedVariants) {
            TrimVariant variant;
            Long variantId = variantDto.getTrimVariantId();

            if (variantId != null) {
                variant = existingVariantsMap.get(variantId);
                if (variant == null) {
                    throw new ResourceNotFoundException("TrimVariant not found with id: " + variantId);
                }
            } else {
                variant = new TrimVariant();
                variant.setTrim(trim);
            }
            variant.setColorCode(variantDto.getColorCode());
            variant.setColorName(variantDto.getColorName());
            variant.setSizeCode(variantDto.getSizeCode());
            variant.setNetPrice(variantDto.getNetPrice());
            variant.setTaxRate(variantDto.getTaxRate());
            variantsToSave.add(variant);
        }

        trim.getVariants().clear();
        trim.getVariants().addAll(variantsToSave);

        Trim savedTrim = trimRepository.save(trim);
        return convertEntityToDto(savedTrim);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            if (trimRepository.existsById(id)) {
                trimVariantRepository.deleteByTrim_TrimId(id);
                trimRepository.deleteById(id);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<TrimDto> search(String keyword) {
        String effectiveKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Trim> trims = trimRepository.searchTrims(effectiveKeyword);
        AtomicInteger index = new AtomicInteger(1);
        return trims.stream()
                .map(trim -> {
                    TrimDto dto = convertEntityToDtoSimple(trim);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Private helper to map data from a DTO to an existing Trim entity.
     * @param dto The source DTO.
     * @param trim The target entity.
     */
    private void mapDtoToEntity(TrimDto dto, Trim trim) {
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + dto.getUnitId()));
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + dto.getSupplierId()));

        trim.setTrimCode(dto.getTrimCode());
        trim.setTrimName(dto.getTrimName());
        trim.setUnit(unit);
        trim.setSupplier(supplier);
        trim.setTechnicalReference(dto.getTechnicalReference());
    }

    /**
     * Private helper to convert a Trim entity to a full DTO, including details.
     * @param trim The source entity.
     * @return A detailed TrimDto.
     */
    private TrimDto convertEntityToDto(Trim trim) {
        TrimDto dto = convertEntityToDtoSimple(trim);
        if (trim.getVariants() != null) {
            dto.setVariants(trim.getVariants().stream()
                    .map(this::convertVariantEntityToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    /**
     * Private helper to convert a Trim entity to a simple DTO for list views.
     * @param trim The source entity.
     * @return A simplified TrimDto.
     */
    private TrimDto convertEntityToDtoSimple(Trim trim) {
        TrimDto dto = new TrimDto();
        dto.setTrimId(trim.getTrimId());
        dto.setTrimCode(trim.getTrimCode());
        dto.setTrimName(trim.getTrimName());
        if (trim.getUnit() != null) {
            dto.setUnitId(trim.getUnit().getUnitId());
            dto.setUnitName(trim.getUnit().getUnitName());
        }
        if (trim.getSupplier() != null) {
            dto.setSupplierId(trim.getSupplier().getSupplierId());
            dto.setSupplierName(trim.getSupplier().getSupplierName());
        }
        dto.setTechnicalReference(trim.getTechnicalReference());
        return dto;
    }

    /**
     * Private helper to convert a TrimVariant entity to its DTO representation.
     * @param variant The source entity.
     * @return A TrimVariantDto.
     */
    private TrimVariantDto convertVariantEntityToDto(TrimVariant variant) {
        TrimVariantDto dto = new TrimVariantDto();
        dto.setTrimVariantId(variant.getTrimVariantId());
        dto.setColorCode(variant.getColorCode());
        dto.setColorName(variant.getColorName());
        dto.setSizeCode(variant.getSizeCode());
        dto.setNetPrice(variant.getNetPrice());
        dto.setTaxRate(variant.getTaxRate());
        return dto;
    }
}
