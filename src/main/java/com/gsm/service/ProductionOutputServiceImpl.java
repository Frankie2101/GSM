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
    private final UserRepository userRepository;

    @Autowired
    public ProductionOutputServiceImpl(ProductionOutputRepository productionOutputRepository,
                                       SaleOrderRepository saleOrderRepository,
                                       UserRepository userRepository) {
        this.productionOutputRepository = productionOutputRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.userRepository = userRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<ProductionOutputDto> findAll() {
        return this.search(null, null, null, null, null);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ProductionOutputDto findById(Long id) {
        ProductionOutput output = productionOutputRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production Output not found with id: " + id));
        return convertEntityToDto(output);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ProductionOutputDto save(ProductionOutputDto dto) {
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

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<ProductionOutputDto> search(String keyword, LocalDate outputDateFrom, LocalDate outputDateTo, String department, String productionLine) {
        // Step 1: Execute the primary search query to get the list of production outputs.
        List<ProductionOutput> outputs = productionOutputRepository.search(keyword, outputDateFrom, outputDateTo, department, productionLine);
        if (outputs.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 2: Collect all unique user IDs from the search results.
        // Using a Set automatically handles uniqueness.
        Set<Long> userIds = outputs.stream()
                .map(ProductionOutput::getCreatedBy)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // Step 3: Fetch all required User entities in a single database query.
        // This is the core of the N+1 problem fix.
        Map<Long, String> userIdToNameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUserName));

        // Step 4: Map the entities to DTOs, using the in-memory map for a fast username lookup.
        AtomicInteger index = new AtomicInteger(1);
        return outputs.stream()
                .map(output -> {
                    ProductionOutputDto dto = convertEntityToDto(output, userIdToNameMap);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        // Using deleteAllById is efficient for bulk deletion when cascading is not complex.
        productionOutputRepository.deleteAllById(ids);
    }

    /**
     * Converts an entity to a DTO using a pre-fetched user map.
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

        if (output.getCreatedBy() != null) {
            dto.setCreatedBy(userMap.getOrDefault(output.getCreatedBy(), String.valueOf(output.getCreatedBy())));
        }

        return dto;
    }

    /**
     * [For Single Records] Converts an entity to a DTO, fetching the username directly.
     */
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

        if (output.getCreatedBy() != null) {
            userRepository.findById(output.getCreatedBy())
                    .ifPresent(user -> dto.setCreatedBy(user.getUserName()));
        }

        if (dto.getCreatedBy() == null && output.getCreatedBy() != null) {
            dto.setCreatedBy(String.valueOf(output.getCreatedBy()));
        }

        return dto;
    }
}