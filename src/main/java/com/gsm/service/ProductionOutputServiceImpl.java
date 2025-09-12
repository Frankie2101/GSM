package com.gsm.service;

import com.gsm.dto.ProductionOutputDto;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.ProductionOutput;
import com.gsm.model.SaleOrder;
import com.gsm.model.User; // Thêm import User
import com.gsm.repository.ProductionOutputRepository;
import com.gsm.repository.SaleOrderRepository;
import com.gsm.repository.UserRepository; // Thêm import UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map; // Thêm import Map
import java.util.Set; // Thêm import Set
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ProductionOutputServiceImpl implements ProductionOutputService {

    private final ProductionOutputRepository productionOutputRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final UserRepository userRepository; // YÊU CẦU MỚI: Inject UserRepository

    @Autowired
    public ProductionOutputServiceImpl(ProductionOutputRepository productionOutputRepository,
                                       SaleOrderRepository saleOrderRepository,
                                       UserRepository userRepository) { // Cập nhật constructor
        this.productionOutputRepository = productionOutputRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionOutputDto> findAll() {
        return this.search(null, null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionOutputDto findById(Long id) {
        ProductionOutput output = productionOutputRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production Output not found with id: " + id));
        // Khi chỉ tìm 1 bản ghi, ta có thể tìm user trực tiếp
        return convertEntityToDto(output);
    }

    @Override
    @Transactional
    public ProductionOutputDto save(ProductionOutputDto dto) {
        // ... (phần logic save giữ nguyên)
        SaleOrder saleOrder = saleOrderRepository.findBySaleOrderNo(dto.getSaleOrderNo())
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with No: " + dto.getSaleOrderNo()));

        ProductionOutput output;
        if (dto.getProductionOutputId() != null) {
            output = productionOutputRepository.findById(dto.getProductionOutputId())
                    .orElseThrow(() -> new ResourceNotFoundException("Production Output not found for ID: " + dto.getProductionOutputId()));
        } else {
            output = new ProductionOutput();
        }

        output.setSaleOrder(saleOrder);
        output.setStyle(dto.getStyle());
        output.setColor(dto.getColor());
        output.setDepartment(dto.getDepartment());
        output.setProductionLine(dto.getProductionLine());
        output.setOutputDate(dto.getOutputDate());
        output.setOutputQuantity(dto.getOutputQuantity());

        ProductionOutput savedOutput = productionOutputRepository.save(output);
        return convertEntityToDto(savedOutput);
    }

    // YÊU CẦU MỚI: Viết lại hàm search để lấy thêm UserName
    @Override
    @Transactional(readOnly = true)
    public List<ProductionOutputDto> search(String keyword, LocalDate outputDateFrom, LocalDate outputDateTo, String department, String productionLine) {
        List<ProductionOutput> outputs = productionOutputRepository.search(keyword, outputDateFrom, outputDateTo, department, productionLine);
        if (outputs.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Thu thập tất cả createdBy user IDs
        Set<Long> userIds = outputs.stream()
                .map(ProductionOutput::getCreatedBy)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. Truy vấn một lần duy nhất để lấy thông tin users
        Map<Long, String> userIdToNameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUserName));

        // 3. Map sang DTO và điền UserName
        AtomicInteger index = new AtomicInteger(1);
        return outputs.stream()
                .map(output -> {
                    ProductionOutputDto dto = convertEntityToDto(output, userIdToNameMap); // Dùng hàm convert mới
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        productionOutputRepository.deleteAllById(ids);
    }

    /**
     * Helper method mới: Chuyển đổi Entity sang DTO và sử dụng Map để tra cứu UserName.
     * Dùng cho danh sách.
     */
    private ProductionOutputDto convertEntityToDto(ProductionOutput output, Map<Long, String> userMap) {
        ProductionOutputDto dto = new ProductionOutputDto();
        dto.setProductionOutputId(output.getProductionOutputId());
        if (output.getSaleOrder() != null) {
            dto.setSaleOrderId(output.getSaleOrder().getSaleOrderId());
            dto.setSaleOrderNo(output.getSaleOrder().getSaleOrderNo());
        }
        dto.setStyle(output.getStyle());
        dto.setColor(output.getColor());
        dto.setDepartment(output.getDepartment());
        dto.setProductionLine(output.getProductionLine());
        dto.setOutputDate(output.getOutputDate());
        dto.setOutputQuantity(output.getOutputQuantity());

        // Lấy UserName từ map
        if (output.getCreatedBy() != null) {
            dto.setCreatedBy(userMap.getOrDefault(output.getCreatedBy(), String.valueOf(output.getCreatedBy())));
        }

        return dto;
    }

    /**
     * Helper method cũ: Giữ lại để dùng cho trường hợp tìm chi tiết 1 bản ghi.
     * Sẽ tốn thêm 1 query nhưng chấp nhận được vì chỉ gọi cho 1 record.
     */
    private ProductionOutputDto convertEntityToDto(ProductionOutput output) {
        ProductionOutputDto dto = new ProductionOutputDto();
        // ... (copy toàn bộ phần map các trường khác như trên)
        dto.setProductionOutputId(output.getProductionOutputId());
        if (output.getSaleOrder() != null) {
            dto.setSaleOrderId(output.getSaleOrder().getSaleOrderId());
            dto.setSaleOrderNo(output.getSaleOrder().getSaleOrderNo());
        }
        dto.setStyle(output.getStyle());
        dto.setColor(output.getColor());
        dto.setDepartment(output.getDepartment());
        dto.setProductionLine(output.getProductionLine());
        dto.setOutputDate(output.getOutputDate());
        dto.setOutputQuantity(output.getOutputQuantity());

        // Tìm UserName trực tiếp
        if (output.getCreatedBy() != null) {
            userRepository.findById(output.getCreatedBy())
                    .ifPresent(user -> dto.setCreatedBy(user.getUserName()));
        }

        // Fallback nếu không tìm thấy user
        if (dto.getCreatedBy() == null && output.getCreatedBy() != null) {
            dto.setCreatedBy(String.valueOf(output.getCreatedBy()));
        }

        return dto;
    }
}