package com.gsm.service;

import com.gsm.dto.BOMTemplateDetailDto;
import com.gsm.dto.BOMTemplateDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.*;
import com.gsm.repository.BOMTemplateRepository;
import com.gsm.repository.FabricRepository;
import com.gsm.repository.ProductCategoryRepository;
import com.gsm.repository.TrimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The concrete implementation of the BOMTemplateService interface.
 * Contains all business logic for managing BOM Templates.
 */
@Service
public class BOMTemplateServiceImpl implements BOMTemplateService {

    private final BOMTemplateRepository bomTemplateRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final FabricRepository fabricRepository;
    private final TrimRepository trimRepository;
    @Autowired
    public BOMTemplateServiceImpl(BOMTemplateRepository bomTemplateRepository,
                                  ProductCategoryRepository productCategoryRepository,
                                  FabricRepository fabricRepository,
                                  TrimRepository trimRepository) {
        this.bomTemplateRepository = bomTemplateRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.fabricRepository = fabricRepository;
        this.trimRepository = trimRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BOMTemplateDto findById(Long id) {
        BOMTemplate template = bomTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BOM Template not found with id: " + id));
        return convertEntityToDto(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BOMTemplateDto> findAll() {
        AtomicInteger index = new AtomicInteger(1);
        return bomTemplateRepository.findAll().stream()
                .map(template -> {
                    BOMTemplateDto dto = convertEntityToDtoSimple(template);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BOMTemplateDto> search(String keyword) {
        String effectiveKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<BOMTemplate> templates = bomTemplateRepository.search(effectiveKeyword);
        AtomicInteger index = new AtomicInteger(1);
        return templates.stream()
                .map(template -> {
                    BOMTemplateDto dto = convertEntityToDtoSimple(template);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        bomTemplateRepository.deleteAllById(ids);
    }

    /**
     * Saves a BOM Template, including its detail lines.
     * This involves checking for duplicates, mapping DTOs to entities,
     * and synchronizing the child collection of details.
     */
    @Override
    @Transactional
    public BOMTemplateDto save(BOMTemplateDto dto) {
        // Check for duplicate code.
        bomTemplateRepository.findByBomTemplateCode(dto.getBomTemplateCode()).ifPresent(existing -> {
            if (dto.getBomTemplateId() == null || !existing.getBomTemplateId().equals(dto.getBomTemplateId())) {
                throw new DuplicateResourceException("BOM Template Code '" + dto.getBomTemplateCode() + "' already exists.");
            }
        });

        // Find existing or create a new template entity.
        BOMTemplate template = (dto.getBomTemplateId() != null)
                ? bomTemplateRepository.findById(dto.getBomTemplateId()).orElseGet(BOMTemplate::new)
                : new BOMTemplate();

        mapDtoToEntity(dto, template);

        // Process and map the detail lines from DTOs to entities.
        List<BOMTemplateDetail> detailsToSave = new ArrayList<>();
        if (dto.getDetails() != null) {
            for (BOMTemplateDetailDto detailDto : dto.getDetails()) {
                BOMTemplateDetail detail = new BOMTemplateDetail();
                detail.setSeq(detailDto.getSeq());
                detail.setRmType(detailDto.getRmType());
                detail.setUsageValue(detailDto.getUsageValue());
                detail.setWaste(detailDto.getWaste());

                // Based on the material type, find and link the correct Fabric or Trim entity.
                if ("FA".equals(detailDto.getRmType())) {
                    Fabric fabric = fabricRepository.findById(detailDto.getRmId())
                            .orElseThrow(() -> new ResourceNotFoundException("Fabric not found with ID: " + detailDto.getRmId()));
                    detail.setFabric(fabric);
                    detail.setMaterialGroup(fabric.getMaterialGroup());
                } else if ("TR".equals(detailDto.getRmType())) {
                    Trim trim = trimRepository.findById(detailDto.getRmId())
                            .orElseThrow(() -> new ResourceNotFoundException("Trim not found with ID: " + detailDto.getRmId()));
                    detail.setTrim(trim);
                    detail.setMaterialGroup(trim.getMaterialGroup());
                }
                detailsToSave.add(detail);
            }
        }

        // Synchronize the details collection: clear old details and add all new ones.
        template.getDetails().clear();
        detailsToSave.forEach(template::addDetail);

        BOMTemplate savedTemplate = bomTemplateRepository.save(template);
        return convertEntityToDto(savedTemplate);
    }


    private void mapDtoToEntity(BOMTemplateDto dto, BOMTemplate template) {
        ProductCategory category = productCategoryRepository.findById(dto.getProductCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Category not found with ID: " + dto.getProductCategoryId()));

        template.setBomTemplateCode(dto.getBomTemplateCode());
        template.setBomTemplateName(dto.getBomTemplateName());
        template.setProductCategory(category);
    }

    private BOMTemplateDto convertEntityToDto(BOMTemplate template) {
        BOMTemplateDto dto = convertEntityToDtoSimple(template);
        dto.setProductCategoryId(template.getProductCategory().getCategoryId());
        if (template.getDetails() != null) {
            dto.setDetails(template.getDetails().stream()
                    .map(this::convertDetailEntityToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private BOMTemplateDetailDto convertDetailEntityToDto(BOMTemplateDetail detail) {
        BOMTemplateDetailDto dto = new BOMTemplateDetailDto();
        dto.setBomTemplateDetailId(detail.getBomTemplateDetailId());
        dto.setSeq(detail.getSeq());
        dto.setRmType(detail.getRmType());
        dto.setUsageValue(detail.getUsageValue());
        dto.setWaste(detail.getWaste());

        if (detail.getMaterialGroup() != null) {
            dto.setMaterialGroupId(detail.getMaterialGroup().getMaterialGroupId());
        }

        if ("FA".equals(detail.getRmType()) && detail.getFabric() != null) {
            dto.setRmId(detail.getFabric().getFabricId());
            dto.setRmCode(detail.getFabric().getFabricCode());
            dto.setRmName(detail.getFabric().getFabricName());
            if (detail.getFabric().getUnit() != null) {
                dto.setUnitName(detail.getFabric().getUnit().getUnitName());
            }
        } else if ("TR".equals(detail.getRmType()) && detail.getTrim() != null) {
            dto.setRmId(detail.getTrim().getTrimId());
            dto.setRmCode(detail.getTrim().getTrimCode());
            dto.setRmName(detail.getTrim().getTrimName());
            if (detail.getTrim().getUnit() != null) {
                dto.setUnitName(detail.getTrim().getUnit().getUnitName());
            }
        }
        return dto;
    }

    private BOMTemplateDto convertEntityToDtoSimple(BOMTemplate template) {
        BOMTemplateDto dto = new BOMTemplateDto();
        dto.setBomTemplateId(template.getBomTemplateId());
        dto.setBomTemplateCode(template.getBomTemplateCode());
        dto.setBomTemplateName(template.getBomTemplateName());
        if (template.getProductCategory() != null) {
            dto.setProductCategoryName(template.getProductCategory().getCategoryName());
        }
        return dto;
    }
}