package com.gsm.service;

import com.gsm.dto.OrderBOMDetailDto;
import com.gsm.dto.OrderBOMDto;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.*;
import com.gsm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderBOMServiceImpl implements OrderBOMService {

    private final OrderBOMRepository orderBOMRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final BOMTemplateRepository bomTemplateRepository;
    private final FabricRepository fabricRepository;
    private final TrimRepository trimRepository;
    private final MaterialGroupRepository materialGroupRepository; // <-- THÊM DÒNG NÀY
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final OrderBOMDetailRepository orderBOMDetailRepository;


    @Autowired
    public OrderBOMServiceImpl(OrderBOMRepository orderBOMRepository,
                               SaleOrderRepository saleOrderRepository,
                               BOMTemplateRepository bomTemplateRepository,
                               FabricRepository fabricRepository,
                               TrimRepository trimRepository,
                               MaterialGroupRepository materialGroupRepository,
                               PurchaseOrderDetailRepository purchaseOrderDetailRepository,
                               // Bổ sung vào constructor
                               PurchaseOrderRepository purchaseOrderRepository,
                               SupplierRepository supplierRepository,
                               OrderBOMDetailRepository orderBOMDetailRepository) {
        this.orderBOMRepository = orderBOMRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.bomTemplateRepository = bomTemplateRepository;
        this.fabricRepository = fabricRepository;
        this.trimRepository = trimRepository;
        this.materialGroupRepository = materialGroupRepository;
        this.purchaseOrderDetailRepository = purchaseOrderDetailRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.orderBOMDetailRepository = orderBOMDetailRepository;
    }

    // === THAY THẾ HOÀN TOÀN PHƯƠNG THỨC NÀY ===
    @Override
    @Transactional
    public Map<String, Object> saveAndGeneratePOs(OrderBOMDto bomDtoFromForm) {
        // BƯỚC 1: LƯU BOM (sử dụng logic save đã có)
        OrderBOMDto savedBom = this.save(bomDtoFromForm);

        // BƯỚC 2: KẾT HỢP DỮ LIỆU ĐỂ LẤY ĐÚNG PURCHASE QTY
        Map<Integer, OrderBOMDetailDto> originalDetailsMap = bomDtoFromForm.getDetails().stream()
                .collect(Collectors.toMap(OrderBOMDetailDto::getSeq, Function.identity()));
        savedBom.getDetails().forEach(savedDetailDto -> {
            OrderBOMDetailDto originalDetailDto = originalDetailsMap.get(savedDetailDto.getSeq());
            if (originalDetailDto != null) {
                savedDetailDto.setPurchaseQty(originalDetailDto.getPurchaseQty());
            }
        });

        // BƯỚC 3: LỌC RA CÁC DÒNG HỢP LỆ ĐỂ TẠO PO
        List<OrderBOMDetailDto> validDetailsForPO = savedBom.getDetails().stream()
                .filter(d -> {
                    // Điều kiện 1: Phải có số lượng mua > 0 và thông tin cơ bản
                    boolean hasBasicInfo = d.getPurchaseQty() != null && d.getPurchaseQty() > 0 &&
                            d.getSupplier() != null && !d.getSupplier().isEmpty() &&
                            d.getPrice() != null && d.getPrice() > 0;
                    if (!hasBasicInfo) return false;

                    // Điều kiện 2: Dòng này CHƯA được tạo PO trước đó
                    OrderBOMDetail tempDetail = new OrderBOMDetail();
                    tempDetail.setOrderBOMDetailId(d.getOrderBOMDetailId());
                    boolean alreadyInPO = purchaseOrderDetailRepository.existsByOrderBOMDetail(tempDetail);

                    return !alreadyInPO; // Chỉ lấy những dòng chưa có trong PO
                })
                .collect(Collectors.toList());

        if (validDetailsForPO.isEmpty()) {
            throw new IllegalStateException("No new valid items found to generate Purchase Orders.");
        }

        // BƯỚC 4: GOM NHÓM VÀ TẠO PO (Logic này giữ nguyên)
        Map<String, List<OrderBOMDetailDto>> groupedBySupplierAndCurrency = validDetailsForPO.stream()
                .collect(Collectors.groupingBy(d -> d.getSupplier() + ":" + d.getCurrency()));

        SaleOrder saleOrder = saleOrderRepository.findById(bomDtoFromForm.getSaleOrderId()).orElseThrow();
        long existingPoCount = purchaseOrderRepository.countBySaleOrderId(saleOrder.getSaleOrderId());
        AtomicLong poSequence = new AtomicLong(existingPoCount + 1);
        int generatedPoCount = 0;

        for (Map.Entry<String, List<OrderBOMDetailDto>> entry : groupedBySupplierAndCurrency.entrySet()) {
            List<OrderBOMDetailDto> detailsForThisPO = entry.getValue();
            Supplier supplier = supplierRepository.findBySupplierName(detailsForThisPO.get(0).getSupplier()).orElseThrow();

            PurchaseOrder po = new PurchaseOrder();
            String poNumber = String.format("%s-%02d", saleOrder.getSaleOrderNo(), poSequence.getAndIncrement());
            po.setPurchaseOrderNo(poNumber);
            po.setSupplier(supplier);
            po.setPoDate(LocalDate.now());
            po.setCurrencyCode(detailsForThisPO.get(0).getCurrency());
            po.setDeliveryTerm(supplier.getDeliveryTerm());
            po.setPaymentTerm(supplier.getPaymentTerm());
            po.setStatus("New");

            for (OrderBOMDetailDto detailDto : detailsForThisPO) {
                PurchaseOrderDetail poDetail = new PurchaseOrderDetail();
                OrderBOMDetail bomDetailRef = orderBOMDetailRepository.findById(detailDto.getOrderBOMDetailId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order BOM Detail not found: " + detailDto.getOrderBOMDetailId()));
                poDetail.setOrderBOMDetail(bomDetailRef);
                poDetail.setPurchaseQuantity((double) Math.round(detailDto.getPurchaseQty()));
                poDetail.setNetPrice(detailDto.getPrice());
                po.addDetail(poDetail);
            }

            purchaseOrderRepository.save(po);
            generatedPoCount++;
        }

        return Map.of("message", "Successfully generated " + generatedPoCount + " new Purchase Order(s).");
    }

    // ==========================================================
    // === PHƯƠNG THỨC MỚI ĐỂ LẤY DANH SÁCH BOM ===
    // ==========================================================
    @Transactional(readOnly = true)
    @Override
    public List<OrderBOMDto> findAll() {
        return orderBOMRepository.findAll().stream()
                .map(this::convertEntityToDtoSimple) // Chỉ cần chuyển đổi đơn giản
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderBOMDto findOrCreateBySaleOrderId(Long saleOrderId) {
        return orderBOMRepository.findBySaleOrder_SaleOrderId(saleOrderId)
                .map(this::convertEntityToDto)
                .orElseGet(() -> {
                    SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                            .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with ID: " + saleOrderId));
                    OrderBOMDto dto = new OrderBOMDto();
                    dto.setSaleOrderId(saleOrder.getSaleOrderId());
                    dto.setSaleOrderNo(saleOrder.getSaleOrderNo());
                    return dto;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public OrderBOMDto generatePreviewFromTemplate(Long saleOrderId, Long bomTemplateId) {
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found: " + saleOrderId));
        BOMTemplate template = bomTemplateRepository.findById(bomTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM Template not found: " + bomTemplateId));

        int totalSoQty = (int) saleOrder.getDetails().stream()
                .mapToDouble(SaleOrderDetail::getOrderQuantity).sum();

        OrderBOMDto orderBOMDto = new OrderBOMDto();
        orderBOMDto.setSaleOrderId(saleOrderId);
        orderBOMDto.setSaleOrderNo(saleOrder.getSaleOrderNo());
        orderBOMDto.setBomTemplateId(bomTemplateId);

        // === LOGIC MỚI: ÁNH XẠ 1-1 TỪ BOM TEMPLATE DETAIL ===
        List<OrderBOMDetailDto> detailDtos = template.getDetails().stream().map(templateDetail -> {
            OrderBOMDetailDto dto = new OrderBOMDetailDto();
            dto.setSeq(templateDetail.getSeq());
            dto.setMaterialType(templateDetail.getRmType());
            dto.setUsageValue(templateDetail.getUsageValue());
            dto.setWaste(templateDetail.getWaste());
            dto.setSoQty(totalSoQty);

            // Lấy thông tin từ Fabric hoặc Trim gốc
            if ("FA".equals(templateDetail.getRmType()) && templateDetail.getFabric() != null) {
                Fabric fabric = templateDetail.getFabric();
                dto.setFabricId(fabric.getFabricId());
                dto.setMaterialCode(fabric.getFabricCode());
                dto.setMaterialName(fabric.getFabricName());
                if (fabric.getUnit() != null) dto.setUom(fabric.getUnit().getUnitName());
                if (fabric.getSupplier() != null) dto.setSupplier(fabric.getSupplier().getSupplierName());
            } else if ("TR".equals(templateDetail.getRmType()) && templateDetail.getTrim() != null) {
                Trim trim = templateDetail.getTrim();
                dto.setTrimId(trim.getTrimId());
                dto.setMaterialCode(trim.getTrimCode());
                dto.setMaterialName(trim.getTrimName());
                if (trim.getUnit() != null) dto.setUom(trim.getUnit().getUnitName());
                if (trim.getSupplier() != null) dto.setSupplier(trim.getSupplier().getSupplierName());
            }

            // Để trống Color, Size, Price để người dùng chọn
            // Tính toán các loại số lượng
            double demandQty = totalSoQty * dto.getUsageValue() * (1 + dto.getWaste() / 100.0);
            BigDecimal roundedDemandQty = new BigDecimal(demandQty).setScale(2, RoundingMode.HALF_UP);

            dto.setDemandQty(roundedDemandQty.doubleValue());
            dto.setInventoryQty(0.0); // Mặc định
            dto.setPurchaseQty(demandQty);

            return dto;
        }).collect(Collectors.toList());

        orderBOMDto.setDetails(detailDtos);
        return orderBOMDto;
    }

    private OrderBOMDetailDto createDetailDtoBase(BOMTemplateDetail templateDetail, int totalSoQty) {
        OrderBOMDetailDto dto = new OrderBOMDetailDto();
        dto.setMaterialType(templateDetail.getRmType());
        dto.setUsageValue(templateDetail.getUsageValue());
        dto.setWaste(templateDetail.getWaste());
        dto.setSoQty(totalSoQty);

        // Công thức mới cho Demand Qty
        double usage = dto.getUsageValue();
        double waste = dto.getWaste();
        double demandQty = totalSoQty * usage * (1 + waste / 100.0);
        BigDecimal roundedDemandQty = new BigDecimal(demandQty).setScale(2, RoundingMode.HALF_UP);

        dto.setDemandQty(roundedDemandQty.doubleValue());

        // Công thức mới cho Purchase Qty
        double inventoryQty = 0.0; // Mặc định là 0
        dto.setInventoryQty(inventoryQty);
        double purchaseQty = demandQty - inventoryQty;
        dto.setPurchaseQty(purchaseQty > 0 ? purchaseQty : 0.0);

        return dto;
    }



    @Override
    @Transactional
    public OrderBOMDto save(OrderBOMDto dto) {
        // 1. Lấy các đối tượng cha
        SaleOrder saleOrder = saleOrderRepository.findById(dto.getSaleOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with ID: " + dto.getSaleOrderId()));
        BOMTemplate bomTemplate = bomTemplateRepository.findById(dto.getBomTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("BOM Template not found with ID: " + dto.getBomTemplateId()));

        // 2. Tìm hoặc tạo mới OrderBOM
        OrderBOM orderBOM = orderBOMRepository.findBySaleOrder_SaleOrderId(dto.getSaleOrderId())
                .orElse(new OrderBOM());
        orderBOM.setSaleOrder(saleOrder);
        orderBOM.setBomTemplate(bomTemplate);

        // --- LOGIC ĐỒNG BỘ HÓA MỚI THÔNG MINH HƠN ---

        // 3. Lấy danh sách ID các chi tiết được gửi từ form
        Set<Long> idsFromDto = dto.getDetails().stream()
                .map(OrderBOMDetailDto::getOrderBOMDetailId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4. Xác định các chi tiết cần xóa một cách an toàn
        List<OrderBOMDetail> detailsToRemove = new ArrayList<>();
        for (OrderBOMDetail existingDetail : orderBOM.getDetails()) {
            if (!idsFromDto.contains(existingDetail.getOrderBOMDetailId())) {
                // Dòng này đã bị người dùng xóa trên form.
                // KIỂM TRA: Chỉ thêm vào danh sách xóa nếu nó CHƯA được dùng trong PO.
                if (!purchaseOrderDetailRepository.existsByOrderBOMDetail(existingDetail)) {
                    detailsToRemove.add(existingDetail);
                }
                // Nếu đã được dùng trong PO, chúng ta sẽ im lặng bỏ qua yêu cầu xóa.
            }
        }
        // Xóa các dòng an toàn khỏi collection
        if (!detailsToRemove.isEmpty()) {
            orderBOM.getDetails().removeAll(detailsToRemove);
        }

        // 5. Cập nhật các dòng hiện có và thêm các dòng mới
        Map<Long, OrderBOMDetail> existingDetailsMap = orderBOM.getDetails().stream()
                .collect(Collectors.toMap(OrderBOMDetail::getOrderBOMDetailId, Function.identity()));

        for (OrderBOMDetailDto detailDto : dto.getDetails()) {
            OrderBOMDetail detail = (detailDto.getOrderBOMDetailId() != null)
                    ? existingDetailsMap.get(detailDto.getOrderBOMDetailId())
                    : null;

            if (detail == null) { // Nếu là dòng mới
                detail = new OrderBOMDetail();
                orderBOM.addDetail(detail); // Thêm vào collection của cha
            }
            // Ánh xạ toàn bộ dữ liệu từ DTO sang Entity
            mapDtoToDetailEntity(detailDto, detail);
        }

        OrderBOM savedOrderBOM = orderBOMRepository.save(orderBOM);
        return convertEntityToDto(savedOrderBOM);
    }

    // Thêm phương thức helper này vào trong class OrderBOMServiceImpl
    private void mapDtoToDetailEntity(OrderBOMDetailDto detailDto, OrderBOMDetail detail) {
        detail.setSeq(detailDto.getSeq());
        detail.setMaterialType(detailDto.getMaterialType());
        detail.setMaterialCode(detailDto.getMaterialCode());
        detail.setMaterialName(detailDto.getMaterialName());
        detail.setUom(detailDto.getUom());
        detail.setSupplier(detailDto.getSupplier());
        detail.setPrice(detailDto.getPrice());
        detail.setCurrency(detailDto.getCurrency());
        detail.setUsageValue(detailDto.getUsageValue());
        detail.setWaste(detailDto.getWaste());
        detail.setDemandQuantity(detailDto.getDemandQty());
        detail.setColorCode(detailDto.getColorCode());
        detail.setColorName(detailDto.getColorName());
        detail.setSize(detailDto.getSize());

        if (detailDto.getMaterialGroupId() != null) {
            detail.setMaterialGroup(materialGroupRepository.findById(detailDto.getMaterialGroupId()).orElse(null));
        }
        if ("FA".equals(detailDto.getMaterialType()) && detailDto.getFabricId() != null) {
            detail.setFabric(fabricRepository.findById(detailDto.getFabricId()).orElse(null));
        } else if ("TR".equals(detailDto.getMaterialType()) && detailDto.getTrimId() != null) {
            detail.setTrim(trimRepository.findById(detailDto.getTrimId()).orElse(null));
        }
    }

    private OrderBOMDto convertEntityToDto(OrderBOM orderBOM) {
        OrderBOMDto dto = convertEntityToDtoSimple(orderBOM);

        int totalSoQty = 0;
        if (orderBOM.getSaleOrder() != null && orderBOM.getSaleOrder().getDetails() != null) {
            totalSoQty = orderBOM.getSaleOrder().getDetails().stream()
                    .mapToInt(SaleOrderDetail::getOrderQuantity)
                    .sum();
        }

        if (orderBOM.getDetails() != null) {
            final int finalTotalSoQty = totalSoQty;
            dto.setDetails(orderBOM.getDetails().stream()
                    .map(detail -> {
                        OrderBOMDetailDto detailDto = convertDetailEntityToDto(detail);
                        detailDto.setSoQty(finalTotalSoQty);
                        detailDto.setDemandQty(detail.getDemandQuantity());
                        return detailDto;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private OrderBOMDetailDto convertDetailEntityToDto(OrderBOMDetail detail) {
        OrderBOMDetailDto dto = new OrderBOMDetailDto();
        dto.setOrderBOMDetailId(detail.getOrderBOMDetailId());
        dto.setSeq(detail.getSeq());
        dto.setMaterialType(detail.getMaterialType());
        dto.setMaterialCode(detail.getMaterialCode());
        dto.setMaterialName(detail.getMaterialName());
        dto.setUom(detail.getUom());
        dto.setSupplier(detail.getSupplier());
        dto.setPrice(detail.getPrice());
        dto.setCurrency(detail.getCurrency());
        dto.setUsageValue(detail.getUsageValue());
        dto.setWaste(detail.getWaste());

        dto.setColorCode(detail.getColorCode());
        dto.setColorName(detail.getColorName());
        dto.setSize(detail.getSize());

        if ("FA".equals(detail.getMaterialType()) && detail.getFabric() != null) {
            dto.setFabricId(detail.getFabric().getFabricId());
        } else if ("TR".equals(detail.getMaterialType()) && detail.getTrim() != null) {
            dto.setTrimId(detail.getTrim().getTrimId());
        }

        return dto;
    }

    private OrderBOMDto convertEntityToDtoSimple(OrderBOM orderBOM) {
        OrderBOMDto dto = new OrderBOMDto();
        dto.setOrderBOMId(orderBOM.getOrderBOMId());

        if (orderBOM.getSaleOrder() != null) {
            dto.setSaleOrderId(orderBOM.getSaleOrder().getSaleOrderId());
            dto.setSaleOrderNo(orderBOM.getSaleOrder().getSaleOrderNo());
        }

        if (orderBOM.getBomTemplate() != null) {
            dto.setBomTemplateId(orderBOM.getBomTemplate().getBomTemplateId());
            dto.setBomTemplateName(orderBOM.getBomTemplate().getBomTemplateName());
        }

        return dto;
    }
}