package com.gsm.service;

import com.gsm.dto.ProductionOutputDto;
import com.gsm.model.ProductionOutput;
import com.gsm.repository.ProductionOutputRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ProductionOutputServiceImpl implements ProductionOutputService {

    private final ProductionOutputRepository productionOutputRepository;

    @Autowired
    public ProductionOutputServiceImpl(ProductionOutputRepository productionOutputRepository) {
        this.productionOutputRepository = productionOutputRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionOutputDto> findAll() {
        AtomicInteger index = new AtomicInteger(1);
        return productionOutputRepository.findAll().stream()
                .map(output -> {
                    ProductionOutputDto dto = convertEntityToDto(output);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // YÊU CẦU MỚI: Cập nhật logic hàm search
    @Override
    @Transactional(readOnly = true)
    public List<ProductionOutputDto> search(String keyword, LocalDate outputDateFrom, LocalDate outputDateTo, String department, String productionLine) {
        List<ProductionOutput> outputs = productionOutputRepository.search(keyword, outputDateFrom, outputDateTo, department, productionLine);
        AtomicInteger index = new AtomicInteger(1);
        return outputs.stream()
                .map(output -> {
                    ProductionOutputDto dto = convertEntityToDto(output);
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

    private ProductionOutputDto convertEntityToDto(ProductionOutput output) {
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
        // YÊU CẦU MỚI: Lấy thông tin createdBy từ AuditableEntity
        dto.setCreatedBy(String.valueOf(output.getCreatedBy()));
        return dto;
    }
}
