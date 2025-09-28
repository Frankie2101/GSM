package com.gsm.service;

import com.gsm.dto.SaleOrderDetailDto;
import com.gsm.dto.SaleOrderDto;
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
import com.gsm.repository.OrderBOMRepository;
import com.gsm.repository.ProductionOutputRepository;
import java.util.stream.Stream;

/**
 * The concrete implementation of the {@link SaleOrderService} interface.
 * Handles all business logic for Sale Orders, including the complex pivoting/un-pivoting of order details.
 */
@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    private final SaleOrderRepository saleOrderRepository;
    private final CustomerRepository customerRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderBOMRepository orderBOMRepository;
    private final ProductionOutputRepository productionOutputRepository;


    @Autowired
    public SaleOrderServiceImpl(SaleOrderRepository saleOrderRepository,
                                CustomerRepository customerRepository,
                                ProductVariantRepository productVariantRepository,
                                OrderBOMRepository orderBOMRepository,
                                ProductionOutputRepository productionOutputRepository) {
        this.saleOrderRepository = saleOrderRepository;
        this.customerRepository = customerRepository;
        this.productVariantRepository = productVariantRepository;
        this.orderBOMRepository = orderBOMRepository;
        this.productionOutputRepository = productionOutputRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public SaleOrderDto save(SaleOrderDto dto) {
        // Validation for duplicate Sale Order number
        saleOrderRepository.findBySaleOrderNo(dto.getSaleOrderNo()).ifPresent(existing -> {
            if (dto.getSaleOrderId() == null || !existing.getSaleOrderId().equals(dto.getSaleOrderId())) {
                throw new DuplicateResourceException("Sale Order No '" + dto.getSaleOrderNo() + "' already exists.");
            }
        });

        // Find existing order for update, or create a new one
        SaleOrder order = (dto.getSaleOrderId() != null)
                ? saleOrderRepository.findById(dto.getSaleOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found for ID: " + dto.getSaleOrderId()))
                : new SaleOrder();

        // Automatically generate SO Number if it's a new order and the number is missing
        if (order.getSaleOrderId() == null && (dto.getSaleOrderNo() == null || dto.getSaleOrderNo().isEmpty())) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: ".concat(String.valueOf(dto.getCustomerId()))));
            long count = saleOrderRepository.countByCustomer(customer);
            String sequence = String.format("%05d", count + 1);
            dto.setSaleOrderNo(customer.getCustomerCode() + sequence);
        }

        // Map header information (customer, dates, etc.) from DTO to entity
        mapDtoToEntity(dto, order);

        // Clear existing details to prepare for synchronization. `orphanRemoval` will handle deletions.z`
        order.getDetails().clear();

        // --- UN-PIVOT LOGIC ---
        // Iterate through the DTO details (one row per product/color)
        if (dto.getDetails() != null) {
            for (SaleOrderDetailDto detailDto : dto.getDetails()) {
               if (detailDto.getProductId() == null || detailDto.getQuantities() == null) {
                    continue;
                }

                // Loop through the quantities map (each entry is a size) to create individual entity rows
                for (Map.Entry<String, Integer> quantityEntry : detailDto.getQuantities().entrySet()) {
                    String size = quantityEntry.getKey();
                    Integer quantity = quantityEntry.getValue();

                    if (quantity != null && quantity > 0) {
                        if (detailDto.getVariantIds() == null || detailDto.getPrices() == null) continue;

                        Long variantId = detailDto.getVariantIds().get(size);
                        Double price = detailDto.getPrices().get(size);

                        if (variantId == null) {
                            continue;
                        }

                        ProductVariant variant = productVariantRepository.findById(variantId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found with ID: " + variantId));

                        // Create a normalized SaleOrderDetail entity for each size
                        SaleOrderDetail detail = new SaleOrderDetail();
                        Integer shipQuantity = detailDto.getShipQuantities() != null ? detailDto.getShipQuantities().get(size) : null;
                        detail.setShipQuantity(shipQuantity);
                        detail.setProductVariant(variant);
                        detail.setOrderQuantity(quantity);
                        detail.setPrice(price != null ? price : variant.getPrice());

                        order.addDetail(detail); // Add the normalized row to the parent order
                    }
                }
            }
        }

        SaleOrder savedOrder = saleOrderRepository.saveAndFlush(order);
        return convertEntityToDto(savedOrder);
    }

    /**
     * {@inheritDoc}
     * <p><b>Use Case:</b> Called when a user clicks to view or edit an existing sale order's details.
     */
    @Override
    @Transactional(readOnly = true)
    public SaleOrderDto findById(Long id) {
        SaleOrder order = saleOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with id: " + id));
        return convertEntityToDto(order);
    }


    /**
     * {@inheritDoc}
     * <p><b>Use Case:</b> Called to display the main sale order list page.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderDto> findAll() {
        Set<Long> bomIds = orderBOMRepository.findDistinctSaleOrderIdsInUse();
        Set<Long> outputIds = productionOutputRepository.findDistinctSaleOrderIdsInUse();
        Set<Long> nonDeletableIds = Stream.concat(bomIds.stream(), outputIds.stream()).collect(Collectors.toSet());

        AtomicInteger index = new AtomicInteger(1);
        return saleOrderRepository.findAll().stream()
                .map(order -> {
                    SaleOrderDto dto = convertEntityToDtoSimple(order);
                    dto.setSequenceNumber((long) index.getAndIncrement());

                    if (nonDeletableIds.contains(order.getSaleOrderId())) {
                        dto.setDeletable(false);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p><b>Use Case:</b> Called from the list page when the user enters a term in the search box.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderDto> search(String keyword) {
        Set<Long> bomIds = orderBOMRepository.findDistinctSaleOrderIdsInUse();
        Set<Long> outputIds = productionOutputRepository.findDistinctSaleOrderIdsInUse();
        Set<Long> nonDeletableIds = Stream.concat(bomIds.stream(), outputIds.stream()).collect(Collectors.toSet());

        String effectiveKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<SaleOrder> orders = saleOrderRepository.search(effectiveKeyword);
        AtomicInteger index = new AtomicInteger(1);
        return orders.stream()
                .map(order -> {
                    SaleOrderDto dto = convertEntityToDtoSimple(order);
                    dto.setSequenceNumber((long) index.getAndIncrement());

                    if (nonDeletableIds.contains(order.getSaleOrderId())) {
                        dto.setDeletable(false);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p><b>REFACTORED:</b> This method was changed to iterate through IDs to ensure
     * safe deletion of child entities (e.g., SaleOrderDetail) before deleting the parent SaleOrder.
     * The previous `deleteAllById` was unsafe and could cause foreign key constraint violations.
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        List<String> undeletableOrders = new ArrayList<>();
        for (Long id : ids) {
            boolean isInUse = orderBOMRepository.existsBySaleOrder_SaleOrderId(id) ||
                    productionOutputRepository.existsBySaleOrder_SaleOrderId(id);
            if (isInUse) {
                saleOrderRepository.findById(id).ifPresent(order ->
                        undeletableOrders.add(order.getSaleOrderNo()));
            }
        }

        if (!undeletableOrders.isEmpty()) {
            throw new IllegalStateException("Cannot delete Sale Orders: "
                    + String.join(", ", undeletableOrders)
                    + ". They are in use by an Order BOM or have Production Output.");
        }

        for (Long id : ids) {
            saleOrderRepository.deleteById(id);
        }
    }

    /**
     * {@inheritDoc}
     * <p><b>Use Case:</b> Primarily used by APIs to quickly fetch an order's existence or basic info
     * without loading all its details.
     */
    @Override
    @Transactional(readOnly = true)
    public SaleOrderDto findBySaleOrderNo(String saleOrderNo) {
        SaleOrder order = saleOrderRepository.findBySaleOrderNo(saleOrderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with No: " + saleOrderNo));
        return convertEntityToDtoSimple(order);
    }


    /**
     * Private helper to map data from a DTO to an existing SaleOrder entity.
     * @param dto The source DTO.
     * @param order The target entity.
     */
    private void mapDtoToEntity(SaleOrderDto dto, SaleOrder order) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + dto.getCustomerId()));

        order.setSaleOrderNo(dto.getSaleOrderNo());
        order.setCustomerPO(dto.getCustomerPO());
        order.setCustomer(customer);
        order.setCurrencyCode(dto.getCurrencyCode());
        order.setOrderDate(dto.getOrderDate());
        order.setProductionStartDate(dto.getProductionStartDate());
        order.setProductionEndDate(dto.getProductionEndDate());
        order.setShipDate(dto.getShipDate());
        order.setStatus(dto.getStatus());
    }

    /**
     * Private helper to convert a SaleOrder entity to a full DTO, including pivoted details.
     * @param order The source entity.
     * @return A detailed SaleOrderDto.
     */
    private SaleOrderDto convertEntityToDto(SaleOrder order) {
        SaleOrderDto dto = convertEntityToDtoSimple(order);
        dto.setCustomerPO(order.getCustomerPO());
        dto.setCurrencyCode(order.getCurrencyCode());
        dto.setProductionStartDate(order.getProductionStartDate());
        dto.setProductionEndDate(order.getProductionEndDate());

        if (order.getDetails() != null) {
            // Use a map to group details by product and color.
            Map<String, SaleOrderDetailDto> detailMap = new LinkedHashMap<>();
            for (SaleOrderDetail detail : order.getDetails()) {
                ProductVariant variant = detail.getProductVariant();
                Product product = variant.getProduct();
                String key = product.getProductId() + "_" + variant.getColor();

                // If a DTO for this product/color group doesn't exist, create it.
                SaleOrderDetailDto detailDto = detailMap.computeIfAbsent(key, k -> {
                    SaleOrderDetailDto newDto = new SaleOrderDetailDto();
                    newDto.setProductId(product.getProductId());
                    newDto.setProductName(product.getProductName());
                    newDto.setColor(variant.getColor());
                    newDto.setUnitName(product.getUnit().getUnitName());
                    newDto.setQuantities(new LinkedHashMap<>());
                    newDto.setPrices(new LinkedHashMap<>());
                    newDto.setVariantIds(new LinkedHashMap<>());
                    return newDto;
                });

                // Populate the size-specific maps within the DTO. This is the "pivot" action.
                detailDto.getQuantities().put(variant.getSize(), detail.getOrderQuantity());
                detailDto.getPrices().put(variant.getSize(), detail.getPrice());
                detailDto.getVariantIds().put(variant.getSize(), variant.getProductVariantId());
                if (detailDto.getShipQuantities() == null) {
                    detailDto.setShipQuantities(new LinkedHashMap<>());
                }
                detailDto.getShipQuantities().put(variant.getSize(), detail.getShipQuantity());
            }
            dto.setDetails(new ArrayList<>(detailMap.values()));
        }
        return dto;
    }

    /**
     * Private helper to convert a SaleOrder entity to a simple DTO for list views.
     * This version is optimized for performance as it does not process the detail collection.
     * @param order The source entity.
     * @return A simplified SaleOrderDto.
     */
    private SaleOrderDto convertEntityToDtoSimple(SaleOrder order) {
        SaleOrderDto dto = new SaleOrderDto();
        dto.setSaleOrderId(order.getSaleOrderId());
        dto.setSaleOrderNo(order.getSaleOrderNo());
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getCustomerId());
            dto.setCustomerName(order.getCustomer().getCustomerName());
        }
        dto.setOrderDate(order.getOrderDate());
        dto.setShipDate(order.getShipDate());
        dto.setStatus(order.getStatus());
        return dto;
    }
}
