package com.gsm.service;

import com.gsm.dto.OrderBOMDetailDto;
import com.gsm.dto.OrderBOMDto;
import com.gsm.dto.PurchaseOrderDetailDto;
import com.gsm.dto.PurchaseOrderDto;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.*;
import com.gsm.repository.OrderBOMDetailRepository;
import com.gsm.repository.PurchaseOrderRepository;
import com.gsm.repository.PurchaseOrderDetailRepository;
import com.gsm.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gsm.repository.SaleOrderRepository;
import com.gsm.repository.PurchaseOrderDetailRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * The concrete implementation of the {@link PurchaseOrderService} interface.
 */
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final OrderBOMDetailRepository orderBOMDetailRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;


    @Autowired
    public PurchaseOrderServiceImpl(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository, OrderBOMDetailRepository orderBOMDetailRepository, SaleOrderRepository saleOrderRepository, PurchaseOrderDetailRepository purchaseOrderDetailRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.orderBOMDetailRepository = orderBOMDetailRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.purchaseOrderDetailRepository = purchaseOrderDetailRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Map<String, Object> generatePOsFromOrderBOM(OrderBOMDto bomDto) {

        // A line is valid if it has a purchase quantity > 0 and is NOT already in another PO.
        List<OrderBOMDetailDto> validDetailsForPO = bomDto.getDetails().stream()
                .filter(d -> {
                    boolean hasPurchaseData = d.getPurchaseQty() != null && d.getPurchaseQty() > 0 &&
                            d.getSupplierId() != null;
                    if (!hasPurchaseData) return false;

                    if (d.getOrderBOMDetailId() == null) return true;
                    return !purchaseOrderDetailRepository.existsByOrderBOMDetail_OrderBOMDetailId(d.getOrderBOMDetailId());
                })
                .collect(Collectors.toList());

        if (validDetailsForPO.isEmpty()) {
            throw new IllegalStateException("No new valid items found to generate Purchase Orders.");
        }

        // 2. GROUP by supplier code
        Map<Long, List<OrderBOMDetailDto>> groupedBySupplier = validDetailsForPO.stream()
                .collect(Collectors.groupingBy(OrderBOMDetailDto::getSupplierId));

        // 3. CREATE: Create a PO for each supplier
        SaleOrder saleOrder = saleOrderRepository.findById(bomDto.getSaleOrderId()).orElseThrow();
        long existingPoCount = purchaseOrderRepository.countBySaleOrderId(saleOrder.getSaleOrderId());
        AtomicLong poSequence = new AtomicLong(existingPoCount + 1);
        List<String> newPoNumbers = new ArrayList<>();

        for (Map.Entry<Long, List<OrderBOMDetailDto>> entry : groupedBySupplier.entrySet()) {
            Long supplierId = entry.getKey();
            List<OrderBOMDetailDto> detailsForThisPO = entry.getValue();

            Supplier supplier = supplierRepository.findById(supplierId).orElseThrow();
            PurchaseOrder po = new PurchaseOrder();

            // Generate PO Number
            String poNumber = String.format("%s-%02d", saleOrder.getSaleOrderNo(), poSequence.getAndIncrement());
            po.setPurchaseOrderNo(poNumber);

            // Set header information
            po.setSupplier(supplier);
            po.setPoDate(LocalDate.now());
            po.setCurrencyCode(detailsForThisPO.get(0).getCurrency());
            po.setDeliveryTerm(supplier.getDeliveryTerm());
            po.setPaymentTerm(supplier.getPaymentTerm());
            po.setStatus("New");

            // Add detail lines
            for (OrderBOMDetailDto detailDto : detailsForThisPO) {
                PurchaseOrderDetail poDetail = new PurchaseOrderDetail();
                OrderBOMDetail bomDetailRef = orderBOMDetailRepository.findById(detailDto.getOrderBOMDetailId())
                        .orElseThrow(() -> new ResourceNotFoundException("FATAL: OrderBOMDetail not found during PO generation. ID: " + detailDto.getOrderBOMDetailId()));

                poDetail.setOrderBOMDetail(bomDetailRef);
                poDetail.setPurchaseQuantity(detailDto.getPurchaseQty());
                poDetail.setNetPrice(detailDto.getPrice());

                po.addDetail(poDetail);
            }

            purchaseOrderRepository.save(po);
            newPoNumbers.add(po.getPurchaseOrderNo());
        }
        return Map.of("message", "Successfully generated " + newPoNumbers.size() + " new Purchase Order(s).", "poNumbers", newPoNumbers);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDto> findAll() {
        return purchaseOrderRepository.findAllWithSupplier().stream()
                .map(this::convertEntityToDtoSimple)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDto findById(Long id) {
        // 1. Fetch the PO and all its details from the database in one go.
        PurchaseOrder po = purchaseOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        // 2. Convert the fully loaded entity into a detailed DTO for the form view.
        return convertEntityToDto(po);
    }

    /** {@inheritDoc} */
    private PurchaseOrderDto convertEntityToDto(PurchaseOrder po) {
        PurchaseOrderDto dto = convertEntityToDtoSimple(po);
        if (po.getDetails() != null) {
            List<PurchaseOrderDetailDto> detailDtos = po.getDetails().stream()
                    .map(this::convertDetailEntityToDto)
                    .collect(Collectors.toList());
            dto.setDetails(detailDtos);

            double totalAmount = detailDtos.stream()
                    .mapToDouble(PurchaseOrderDetailDto::getLineAmount)
                    .sum();
            dto.setTotalAmount(totalAmount);

        } else {
            dto.setDetails(Collections.emptyList());
        }
        return dto;
    }

    private PurchaseOrderDetailDto convertDetailEntityToDto(PurchaseOrderDetail detail) {
        if (detail == null) {
            return null;
        }

        PurchaseOrderDetailDto dto = new PurchaseOrderDetailDto();
        OrderBOMDetail bomDetail = detail.getOrderBOMDetail(); // Không cần kiểm tra null ở đây

        dto.setOrderBOMDetailId(bomDetail.getOrderBOMDetailId());
        dto.setMaterialType(bomDetail.getMaterialType());
        dto.setColorCode(bomDetail.getColorCode());
        dto.setSize(bomDetail.getSize());
        dto.setUom(bomDetail.getUom());

        if (bomDetail.getMaterialGroup() != null) {
            dto.setMaterialGroupId(bomDetail.getMaterialGroup().getMaterialGroupId());
            dto.setMaterialGroupName(bomDetail.getMaterialGroup().getMaterialGroupName()); // ✅ Đã sửa
        }

        if ("FA".equals(bomDetail.getMaterialType()) && bomDetail.getFabric() != null) {
            dto.setFabricId(bomDetail.getFabric().getFabricId());
            dto.setMaterialCode(bomDetail.getFabric().getFabricCode());
            dto.setMaterialName(bomDetail.getFabric().getFabricName());
        } else if ("TR".equals(bomDetail.getMaterialType()) && bomDetail.getTrim() != null) {
            dto.setTrimId(bomDetail.getTrim().getTrimId());
            dto.setMaterialCode(bomDetail.getTrim().getTrimCode());
            dto.setMaterialName(bomDetail.getTrim().getTrimName());
        }

        dto.setPurchaseOrderDetailId(detail.getPurchaseOrderDetailId());
        dto.setPurchaseQuantity(detail.getPurchaseQuantity());
        dto.setNetPrice(detail.getNetPrice());
        dto.setTaxRate(detail.getTaxRate());
        dto.setReceivedQuantity(detail.getReceivedQuantity());

        Double quantity = detail.getPurchaseQuantity() != null ? detail.getPurchaseQuantity() : 0.0;
        Double price = detail.getNetPrice() != null ? detail.getNetPrice() : 0.0;
        Double tax = detail.getTaxRate() != null ? detail.getTaxRate() : 0.0;
        dto.setLineAmount(quantity * price * (1 + tax / 100.0));

        return dto;
    }

    private PurchaseOrderDto convertEntityToDtoSimple(PurchaseOrder po) {
        if (po == null) {
            return null;
        }
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setPurchaseOrderId(po.getPurchaseOrderId());
        dto.setPurchaseOrderNo(po.getPurchaseOrderNo());
        dto.setPoDate(po.getPoDate());
        dto.setArrivalDate(po.getArrivalDate());
        dto.setStatus(po.getStatus());
        dto.setCurrencyCode(po.getCurrencyCode());
        dto.setDeliveryTerm(po.getDeliveryTerm());
        dto.setPaymentTerm(po.getPaymentTerm());

        if (po.getSupplier() != null) {
            dto.setSupplierName(po.getSupplier().getSupplierName());
            dto.setSupplierId(po.getSupplier().getSupplierId());
        } else {
            dto.setSupplierName("N/A");
        }

        double totalAmount = 0.0;
        if (po.getDetails() != null) {
            totalAmount = po.getDetails().stream()
                    .mapToDouble(d -> (d != null && d.getPurchaseQuantity() != null && d.getNetPrice() != null)
                            ? d.getPurchaseQuantity() * d.getNetPrice()
                            : 0)
                    .sum();
        }
        dto.setTotalAmount(totalAmount);

        return dto;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public PurchaseOrderDto save(PurchaseOrderDto dto) {
        // Step 1: Find the existing PO entity for an update, or create a new one for insertion.
        PurchaseOrder po = (dto.getPurchaseOrderId() != null)
                ? purchaseOrderRepository.findByIdWithDetails(dto.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PO not found: " + dto.getPurchaseOrderId()))
                : new PurchaseOrder();

        // Step 2: Map all header-level data (supplier, dates, terms) from the DTO to the entity.
        mapDtoToEntityHeader(dto, po);

        // Step 3: Create a Map to find the fields with ID
        Map<Long, PurchaseOrderDetail> existingDetailsMap = po.getDetails().stream()
                .collect(Collectors.toMap(PurchaseOrderDetail::getPurchaseOrderDetailId, detail -> detail));

        // Step 4: Loop through the new detail DTOs from the form and create new detail entities.
        if (dto.getDetails() != null) {
            for (PurchaseOrderDetailDto detailDto : dto.getDetails()) {
                PurchaseOrderDetail existingDetail = existingDetailsMap.get(detailDto.getPurchaseOrderDetailId());

                if (existingDetail != null) {
                    existingDetail.setPurchaseQuantity(detailDto.getPurchaseQuantity());
                    existingDetail.setNetPrice(detailDto.getNetPrice());
                    existingDetail.setTaxRate(detailDto.getTaxRate());
                    existingDetail.setReceivedQuantity(detailDto.getReceivedQuantity() != null ? detailDto.getReceivedQuantity() : 0.0);
                }
            }
        }

        // Step 5: Save the parent PO. JPA will automatically handle all INSERT, UPDATE, and DELETE.
        PurchaseOrder savedPO = purchaseOrderRepository.save(po);
        // Step 6: Convert the saved entity back to a DTO to return to the client.
        return convertEntityToDto(savedPO);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            if (purchaseOrderRepository.existsById(id)) {
                purchaseOrderRepository.deleteById(id);
            }
        }
    }

    @Override
    @Transactional
    public void submitForApproval(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found: " + id));

        if ("New".equals(po.getStatus()) || "Rejected".equals(po.getStatus())) {
            po.setStatus("Submitted");
            purchaseOrderRepository.save(po);
        } else {
            throw new IllegalStateException("Only POs with status 'New' or 'Rejected' can be submitted.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDto> findPendingApproval() {
        return purchaseOrderRepository.findByStatus("Submitted").stream()
                .map(this::convertEntityToDtoSimple)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approve(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found: " + id));
        if ("Submitted".equals(po.getStatus())) {
            po.setStatus("Approved");
            purchaseOrderRepository.save(po);
        } else {
            throw new IllegalStateException("Only 'Submitted' orders can be approved.");
        }
    }

    @Override
    @Transactional
    public void reject(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found: " + id));
        if ("Submitted".equals(po.getStatus())) {
            po.setStatus("Rejected");
            purchaseOrderRepository.save(po);
        } else {
            throw new IllegalStateException("Only 'Submitted' orders can be rejected.");
        }
    }

    private void mapDtoToEntityHeader(PurchaseOrderDto dto, PurchaseOrder po) {
        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + dto.getSupplierId()));
            po.setSupplier(supplier);
        }
        po.setPurchaseOrderNo(dto.getPurchaseOrderNo());
        po.setPoDate(dto.getPoDate() == null ? LocalDate.now() : dto.getPoDate());
        po.setArrivalDate(dto.getArrivalDate());
        po.setCurrencyCode(dto.getCurrencyCode());
        po.setDeliveryTerm(dto.getDeliveryTerm());
        po.setPaymentTerm(dto.getPaymentTerm());

        if (po.getPurchaseOrderId() == null) {
            po.setStatus("New");
        }
    }

}