package com.gsm.service;

import com.gsm.dto.OrderBOMDto;
import com.gsm.dto.PurchaseOrderDetailDto;
import com.gsm.dto.PurchaseOrderDto;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.OrderBOMDetail;
import com.gsm.model.PurchaseOrder;
import com.gsm.model.PurchaseOrderDetail;
import com.gsm.model.Supplier;
import com.gsm.repository.OrderBOMDetailRepository;
import com.gsm.repository.PurchaseOrderRepository;
import com.gsm.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final OrderBOMDetailRepository orderBOMDetailRepository;

    @Autowired
    public PurchaseOrderServiceImpl(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository, OrderBOMDetailRepository orderBOMDetailRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.orderBOMDetailRepository = orderBOMDetailRepository; // Gán giá trị
    }

    // ... các phương thức khác giữ nguyên ...
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDto> findAll() {
        return purchaseOrderRepository.findAllWithSupplier().stream()
                .map(this::convertEntityToDtoSimple)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDto findById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
        return convertEntityToDto(po);
    }

    // === CÁC HÀM HELPER MAPPING (PRIVATE) ===

    private PurchaseOrderDto convertEntityToDto(PurchaseOrder po) {
        PurchaseOrderDto dto = convertEntityToDtoSimple(po);
        if (po.getDetails() != null) {
            List<PurchaseOrderDetailDto> detailDtos = po.getDetails().stream()
                    .map(this::convertDetailEntityToDto) // Sử dụng hàm đã sửa lỗi
                    .collect(Collectors.toList());
            dto.setDetails(detailDtos);

            // TÍNH LẠI TỔNG TIỀN TỪ CÁC DÒNG DETAIL ĐÃ CÓ lineAmount
            double totalAmount = detailDtos.stream()
                    .mapToDouble(PurchaseOrderDetailDto::getLineAmount)
                    .sum();
            dto.setTotalAmount(totalAmount);

        } else {
            dto.setDetails(Collections.emptyList());
        }
        return dto;
    }

    /**
     * SỬA LỖI QUAN TRỌNG: Lấy dữ liệu thật từ entity `PurchaseOrderDetail`
     * thay vì dữ liệu giả "CODE-XXX".
     * Giả định entity của bạn có các trường tương ứng.
     */
    // trong file: com/gsm/service/PurchaseOrderServiceImpl.java

// ... các imports và các phương thức khác ...

    private PurchaseOrderDetailDto convertDetailEntityToDto(PurchaseOrderDetail detail) {
        if (detail == null) return null;

        PurchaseOrderDetailDto dto = new PurchaseOrderDetailDto();

        // Lấy dữ liệu số lượng, giá và tính toán thành tiền (giữ nguyên)
        Double quantity = detail.getPurchaseQuantity() != null ? detail.getPurchaseQuantity() : 0.0;
        Double price = detail.getNetPrice() != null ? detail.getNetPrice() : 0.0;
        Double tax = detail.getTaxRate() != null ? detail.getTaxRate() : 0.0;
        Double lineAmount = quantity * price * (1 + tax / 100.0);

        // --- PHẦN SỬA LỖI ---
        OrderBOMDetail bomDetail = detail.getOrderBOMDetail();
        Long fabricId = null;
        Long trimId = null;
        String materialCode = "N/A"; // Giá trị mặc định

        if (bomDetail != null) {
            // Lấy thông tin chung từ BOM Detail
            dto.setOrderBOMDetailId(bomDetail.getOrderBOMDetailId());
            dto.setMaterialType(bomDetail.getMaterialType());
            dto.setMaterialName(bomDetail.getMaterialName());
            dto.setColorCode(bomDetail.getColorCode());
            dto.setSize(bomDetail.getSize());
            dto.setUom(bomDetail.getUom());

            // Lấy ID và MÃ CODE từ đối tượng Fabric hoặc Trim gốc để đảm bảo chính xác
            if ("FA".equals(bomDetail.getMaterialType()) && bomDetail.getFabric() != null) {
                fabricId = bomDetail.getFabric().getFabricId();
                materialCode = bomDetail.getFabric().getFabricCode(); // Lấy mã code thật
            } else if ("TR".equals(bomDetail.getMaterialType()) && bomDetail.getTrim() != null) {
                trimId = bomDetail.getTrim().getTrimId();
                materialCode = bomDetail.getTrim().getTrimCode(); // Lấy mã code thật
            }
        }

        // Gán các giá trị đã được xử lý vào DTO
        dto.setPurchaseOrderDetailId(detail.getPurchaseOrderDetailId());
        dto.setFabricId(fabricId);
        dto.setTrimId(trimId);
        dto.setMaterialCode(materialCode); // Gán mã code đã được lấy đúng
        dto.setPurchaseQuantity(quantity);
        dto.setNetPrice(price);
        dto.setTaxRate(detail.getTaxRate());
        dto.setReceivedQuantity(detail.getReceivedQuantity());
        dto.setLineAmount(lineAmount);

        return dto;
    }


    // ... các phương thức và code khác giữ nguyên ...
    // ... các phương thức và hàm helper khác giữ nguyên như file bạn đã gửi ...
    private PurchaseOrderDto convertEntityToDtoSimple(PurchaseOrder po) {
        if (po == null) {
            return null;
        }
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setPurchaseOrderId(po.getPurchaseOrderId());
        dto.setPurchaseOrderNo(po.getPurchaseOrderNo());
        dto.setPoDate(po.getPoDate());
        dto.setArrivalDate(po.getArrivalDate()); // Bổ sung
        dto.setStatus(po.getStatus());
        dto.setCurrencyCode(po.getCurrencyCode());
        dto.setDeliveryTerm(po.getDeliveryTerm()); // Bổ sung
        dto.setPaymentTerm(po.getPaymentTerm());   // Bổ sung

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

    @Override
    @Transactional
    public PurchaseOrderDto save(PurchaseOrderDto dto) {
        PurchaseOrder po = (dto.getPurchaseOrderId() != null)
                ? purchaseOrderRepository.findByIdWithDetails(dto.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PO not found: " + dto.getPurchaseOrderId()))
                : new PurchaseOrder();

        if (po.getPurchaseOrderId() != null && !"New".equals(po.getStatus()) && !"Rejected".equals(po.getStatus())) {
            throw new IllegalStateException("Only POs with status 'New' or 'Rejected' can be edited.");
        }

        // 1. Ánh xạ thông tin Header
        mapDtoToEntityHeader(dto, po);

        // 2. Xóa các detail cũ
        po.getDetails().clear();

        // 3. Thêm các detail mới từ DTO
        if (dto.getDetails() != null) {
            for (PurchaseOrderDetailDto detailDto : dto.getDetails()) {
                PurchaseOrderDetail newDetail = new PurchaseOrderDetail();

                // Lấy OrderBOMDetail gốc (quan trọng để truy vết)
                OrderBOMDetail bomDetail = orderBOMDetailRepository.findById(detailDto.getOrderBOMDetailId())
                        .orElseThrow(() -> new ResourceNotFoundException("OrderBOMDetail not found with ID: " + detailDto.getOrderBOMDetailId()));
                newDetail.setOrderBOMDetail(bomDetail);

                // Map các thông tin còn lại
                newDetail.setPurchaseQuantity(detailDto.getPurchaseQuantity());
                newDetail.setNetPrice(detailDto.getNetPrice());
                newDetail.setTaxRate(detailDto.getTaxRate());
                newDetail.setReceivedQuantity(detailDto.getReceivedQuantity() != null ? detailDto.getReceivedQuantity() : 0.0);

                // Thêm detail đã hoàn chỉnh vào PO
                po.addDetail(newDetail);
            }
        }

        PurchaseOrder savedPO = purchaseOrderRepository.save(po);
        return convertEntityToDto(savedPO);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found: " + id));

        if (!"New".equals(po.getStatus()) && !"Rejected".equals(po.getStatus())) {
            throw new IllegalStateException("Only POs with status 'New' or 'Rejected' can be deleted.");
        }
        purchaseOrderRepository.deleteById(id);
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

    // ĐỔI TÊN mapDtoToEntity thành mapDtoToEntityHeader
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

        // Chỉ set status khi tạo mới
        if (po.getPurchaseOrderId() == null) {
            po.setStatus("New");
        }
    }

}