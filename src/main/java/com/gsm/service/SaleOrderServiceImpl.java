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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    private final SaleOrderRepository saleOrderRepository;
    private final CustomerRepository customerRepository;
    private final ProductVariantRepository productVariantRepository;

    @Autowired
    public SaleOrderServiceImpl(SaleOrderRepository saleOrderRepository,
                                CustomerRepository customerRepository,
                                ProductVariantRepository productVariantRepository) {
        this.saleOrderRepository = saleOrderRepository;
        this.customerRepository = customerRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    @Transactional
    public SaleOrderDto save(SaleOrderDto dto) {
        // Phần kiểm tra trùng lặp và tìm/tạo mới SaleOrder giữ nguyên
        saleOrderRepository.findBySaleOrderNo(dto.getSaleOrderNo()).ifPresent(existing -> {
            if (dto.getSaleOrderId() == null || !existing.getSaleOrderId().equals(dto.getSaleOrderId())) {
                throw new DuplicateResourceException("Sale Order No '" + dto.getSaleOrderNo() + "' already exists.");
            }
        });

        SaleOrder order;
        if (dto.getSaleOrderId() != null) {
            order = saleOrderRepository.findById(dto.getSaleOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found for ID: " + dto.getSaleOrderId()));
        } else {
            order = new SaleOrder();
        }

        if (order.getSaleOrderId() == null && (dto.getSaleOrderNo() == null || dto.getSaleOrderNo().isEmpty())) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: ".concat(String.valueOf(dto.getCustomerId()))));
            long count = saleOrderRepository.countByCustomer(customer);
            String sequence = String.format("%05d", count + 1);
            dto.setSaleOrderNo(customer.getCustomerCode() + sequence);
        }

        // Map thông tin header (giữ nguyên)
        mapDtoToEntity(dto, order);

        order.getDetails().clear();

// Bước 2: Duyệt qua các dòng DTO từ form và xử lý an toàn.
        if (dto.getDetails() != null) {
            for (SaleOrderDetailDto detailDto : dto.getDetails()) {
                // Bỏ qua nếu dòng detail này không có productId hoặc không có Map quantities.
                if (detailDto.getProductId() == null || detailDto.getQuantities() == null) {
                    continue;
                }

                // Vòng lặp "un-pivot": Biến các cột size thành các dòng SaleOrderDetail.
                for (Map.Entry<String, Integer> quantityEntry : detailDto.getQuantities().entrySet()) {
                    String size = quantityEntry.getKey();
                    Integer quantity = quantityEntry.getValue();

                    // Chỉ xử lý khi số lượng > 0.
                    if (quantity != null && quantity > 0) {
                        // Lấy ra variantId và price tương ứng với size.
                        // Thêm các bước kiểm tra null để đảm bảo không có lỗi xảy ra.
                        if (detailDto.getVariantIds() == null || detailDto.getPrices() == null) continue;

                        Long variantId = detailDto.getVariantIds().get(size);
                        Double price = detailDto.getPrices().get(size);

                        // Nếu không tìm thấy variantId hoặc price cho size này, bỏ qua.
                        if (variantId == null) {
                            continue; // An toàn là trên hết
                        }



                        // Tìm ProductVariant từ database.
                        ProductVariant variant = productVariantRepository.findById(variantId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found with ID: " + variantId));

                        // Tạo đối tượng SaleOrderDetail và điền đầy đủ thông tin.
                        SaleOrderDetail detail = new SaleOrderDetail();
                        Integer shipQuantity = detailDto.getShipQuantities() != null ? detailDto.getShipQuantities().get(size) : null;
                        detail.setShipQuantity(shipQuantity);
                        detail.setProductVariant(variant);
                        detail.setOrderQuantity(quantity);

                        // Nếu giá từ form là null, có thể lấy giá mặc định từ variant.
                        detail.setPrice(price != null ? price : variant.getPrice());

                        // Thêm chi tiết đã hoàn chỉnh vào đơn hàng.
                        // Phương thức addDetail sẽ tự động thiết lập mối quan hệ hai chiều.
                        order.addDetail(detail);
                    }
                }
            }
        }
// === KẾT THÚC PHẦN SỬA LỖI LOGIC DETAILS ===


        SaleOrder savedOrder = saleOrderRepository.saveAndFlush(order);

        return convertEntityToDto(savedOrder);
    }

    // ... các phương thức khác không thay đổi ...
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

    private SaleOrderDto convertEntityToDto(SaleOrder order) {
        SaleOrderDto dto = convertEntityToDtoSimple(order);
        dto.setCustomerPO(order.getCustomerPO());
        dto.setCurrencyCode(order.getCurrencyCode());
        dto.setProductionStartDate(order.getProductionStartDate());
        dto.setProductionEndDate(order.getProductionEndDate());

        if (order.getDetails() != null) {
            Map<String, SaleOrderDetailDto> detailMap = new LinkedHashMap<>();
            for (SaleOrderDetail detail : order.getDetails()) {
                ProductVariant variant = detail.getProductVariant();
                Product product = variant.getProduct();
                String key = product.getProductId() + "_" + variant.getColor();


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

    @Override
    @Transactional(readOnly = true)
    public SaleOrderDto findById(Long id) {
        SaleOrder order = saleOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with id: " + id));
        return convertEntityToDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderDto> findAll() {
        AtomicInteger index = new AtomicInteger(1);
        return saleOrderRepository.findAll().stream()
                .map(order -> {
                    SaleOrderDto dto = convertEntityToDtoSimple(order);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderDto> search(String keyword) {
        String effectiveKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<SaleOrder> orders = saleOrderRepository.search(effectiveKeyword);
        AtomicInteger index = new AtomicInteger(1);
        return orders.stream()
                .map(order -> {
                    SaleOrderDto dto = convertEntityToDtoSimple(order);
                    dto.setSequenceNumber((long) index.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        saleOrderRepository.deleteAllById(ids);
    }

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
