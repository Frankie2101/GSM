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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderBOMServiceImpl implements OrderBOMService {

    private final OrderBOMRepository orderBOMRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final BOMTemplateRepository bomTemplateRepository;
    private final FabricRepository fabricRepository;
    private final TrimRepository trimRepository;
    private final MaterialGroupRepository materialGroupRepository; // <-- THÊM DÒNG NÀY

    @Autowired
    public OrderBOMServiceImpl(OrderBOMRepository orderBOMRepository,
                               SaleOrderRepository saleOrderRepository,
                               BOMTemplateRepository bomTemplateRepository,
                               FabricRepository fabricRepository,
                               TrimRepository trimRepository,
                               MaterialGroupRepository materialGroupRepository) { // <-- THÊM THAM SỐ NÀY
        this.orderBOMRepository = orderBOMRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.bomTemplateRepository = bomTemplateRepository;
        this.fabricRepository = fabricRepository;
        this.trimRepository = trimRepository;
        this.materialGroupRepository = materialGroupRepository; // <-- THÊM DÒNG NÀY
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
        SaleOrder saleOrder = saleOrderRepository.findById(dto.getSaleOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with ID: " + dto.getSaleOrderId()));
        BOMTemplate bomTemplate = bomTemplateRepository.findById(dto.getBomTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("BOM Template not found with ID: " + dto.getBomTemplateId()));

        OrderBOM orderBOM = orderBOMRepository.findBySaleOrder_SaleOrderId(dto.getSaleOrderId())
                .orElse(new OrderBOM());

        orderBOM.setSaleOrder(saleOrder);
        orderBOM.setBomTemplate(bomTemplate);

        orderBOM.getDetails().clear();
        if (dto.getDetails() != null) {
            for (OrderBOMDetailDto detailDto : dto.getDetails()) {
                OrderBOMDetail detail = new OrderBOMDetail();
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

                // THÊM CÁC TRƯỜNG MỚI
                detail.setColorCode(detailDto.getColorCode());
                detail.setColorName(detailDto.getColorName());
                detail.setSize(detailDto.getSize());

                if (detailDto.getMaterialGroupId() != null) {
                    MaterialGroup mg = materialGroupRepository.findById(detailDto.getMaterialGroupId()).orElse(null);
                    detail.setMaterialGroup(mg);
                }

                if ("FA".equals(detailDto.getMaterialType()) && detailDto.getFabricId() != null) {
                    detail.setFabric(fabricRepository.findById(detailDto.getFabricId()).orElse(null));
                } else if ("TR".equals(detailDto.getMaterialType()) && detailDto.getTrimId() != null) {
                    detail.setTrim(trimRepository.findById(detailDto.getTrimId()).orElse(null));
                }

                orderBOM.addDetail(detail);
            }
        }

        OrderBOM savedOrderBOM = orderBOMRepository.save(orderBOM);
        return convertEntityToDto(savedOrderBOM);
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