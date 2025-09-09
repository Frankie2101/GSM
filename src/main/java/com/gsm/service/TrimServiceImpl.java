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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public TrimDto findById(Long id) {
        Trim trim = trimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trim not found with id: " + id));
        return convertEntityToDto(trim);
    }

    @Override
    @Transactional
    public TrimDto save(TrimDto dto) {
        trimRepository.findByTrimCode(dto.getTrimCode()).ifPresent(existing -> {
            if (dto.getTrimId() == null || !existing.getTrimId().equals(dto.getTrimId())) {
                throw new DuplicateResourceException("Trim Code '" + dto.getTrimCode() + "' already exists.");
            }
        });

        Trim trim = (dto.getTrimId() != null)
                ? trimRepository.findById(dto.getTrimId()).orElseGet(Trim::new)
                : new Trim();

        mapDtoToEntity(dto, trim);

        // Logic xử lý dải size
        List<TrimVariantDto> processedVariants = new ArrayList<>();
        if (dto.getVariants() != null) {
            dto.getVariants().forEach(variantDto -> {
                String[] sizeCodes = variantDto.getSizeCode().split("\\s*,\\s*");
                Arrays.stream(sizeCodes)
                        .filter(size -> !size.trim().isEmpty())
                        .forEach(size -> {
                            TrimVariantDto newDto = new TrimVariantDto();
                            newDto.setTrimVariantId(variantDto.getTrimVariantId());
                            newDto.setColorCode(variantDto.getColorCode());
                            newDto.setColorName(variantDto.getColorName());
                            newDto.setSizeCode(size.trim());
                            newDto.setNetPrice(variantDto.getNetPrice());
                            newDto.setTaxRate(variantDto.getTaxRate());
                            processedVariants.add(newDto);
                        });
            });
        }

        // Logic đồng bộ hóa
        List<TrimVariant> variantsToSave = new ArrayList<>();
        for(TrimVariantDto variantDto : processedVariants) {
            TrimVariant variant = new TrimVariant();
            // Logic tìm variant cũ nếu có ID sẽ phức tạp hơn khi tách size,
            // nên ta dùng chiến lược xóa hết thêm lại cho đơn giản và an toàn.
            variant.setColorCode(variantDto.getColorCode());
            variant.setColorName(variantDto.getColorName());
            variant.setSizeCode(variantDto.getSizeCode());
            variant.setNetPrice(variantDto.getNetPrice());
            variant.setTaxRate(variantDto.getTaxRate());
            variantsToSave.add(variant);
        }

        trim.getVariants().clear();
        variantsToSave.forEach(trim::addVariant);

        Trim savedTrim = trimRepository.save(trim);
        return convertEntityToDto(savedTrim);
    }

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

    private TrimDto convertEntityToDto(Trim trim) {
        TrimDto dto = convertEntityToDtoSimple(trim);
        if (trim.getVariants() != null) {
            dto.setVariants(trim.getVariants().stream()
                    .map(this::convertVariantEntityToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

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
