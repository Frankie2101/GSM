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

    @Override
    @Transactional(readOnly = true)
    public FabricDto findById(Long id) {
        Fabric fabric = fabricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fabric not found with id: " + id));
        return convertEntityToDto(fabric);
    }

    @Override
    @Transactional
    public FabricDto save(FabricDto dto) {
        // 1. Kiểm tra mã vải đã tồn tại hay chưa
        fabricRepository.findByFabricCode(dto.getFabricCode()).ifPresent(existing -> {
            if (dto.getFabricId() == null || !existing.getFabricId().equals(dto.getFabricId())) {
                throw new DuplicateResourceException("Fabric Code '" + dto.getFabricCode() + "' already exists.");
            }
        });

        // 2. Tìm hoặc tạo mới entity Fabric
        Fabric fabric = (dto.getFabricId() != null)
                ? fabricRepository.findById(dto.getFabricId())
                .orElseThrow(() -> new ResourceNotFoundException("Fabric not found with id: " + dto.getFabricId()))
                : new Fabric();

        // 3. Map dữ liệu từ DTO sang Entity
        mapDtoToEntity(dto, fabric);

        // 4. --- LOGIC ĐỒNG BỘ FABRIC COLORS CHÍNH XÁC ---

        // Tạo một Map chứa các màu đang có trong DB để dễ dàng truy xuất
        Map<Long, FabricColor> existingColorsMap = fabric.getFabricColors().stream()
                .collect(Collectors.toMap(FabricColor::getFabricColorId, color -> color));

        List<FabricColor> updatedColors = new ArrayList<>();

        if (dto.getFabricColors() != null) {
            for (FabricColorDto colorDto : dto.getFabricColors()) {
                FabricColor color;
                Long colorId = colorDto.getFabricColorId();

                // A. Nếu màu từ form có ID -> Đây là màu CŨ, cần UPDATE
                if (colorId != null) {
                    color = existingColorsMap.get(colorId);
                    if (color == null) {
                        // Xử lý trường hợp ID không hợp lệ nếu cần
                        throw new ResourceNotFoundException("FabricColor not found with id: " + colorId);
                    }
                }
                // B. Nếu màu từ form KHÔNG có ID -> Đây là màu MỚI, cần ADD
                else {
                    color = new FabricColor();
                    color.setFabric(fabric);
                }

                // Cập nhật/gán thông tin cho màu (cũ hoặc mới)
                color.setColor(colorDto.getColor());
                color.setColorName(colorDto.getColorName());
                color.setWidth(colorDto.getWidth());
                color.setNetPrice(colorDto.getNetPrice());
                color.setTaxPercent(colorDto.getTaxPercent());
                updatedColors.add(color);
            }
        }

        // Thao tác trên collection gốc của entity để `orphanRemoval` hoạt động
        // JPA sẽ tự động phát hiện những màu nào bị thiếu và xóa chúng khỏi DB.
        fabric.getFabricColors().clear();
        fabric.getFabricColors().addAll(updatedColors);

        // 5. Lưu lại Fabric, JPA sẽ tự động xử lý tất cả các thay đổi
        Fabric savedFabric = fabricRepository.save(fabric);

        // Xả (flush) để đảm bảo dữ liệu được ghi xuống DB trước khi đọc lại
        fabricRepository.flush();

        return convertEntityToDto(savedFabric);
    }

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

    // --- CÁC PHƯƠNG THỨC CONVERT HELPER ĐÃ SỬA LỖI ---

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

    // Phương thức này dùng cho trang danh sách (không cần chi tiết màu)
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

    // Phương thức helper để convert FabricColor sang DTO
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
