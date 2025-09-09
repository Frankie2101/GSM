package com.gsm.service;

import com.gsm.dto.dashboard.*;
import com.gsm.enums.SaleOrderStatus;
import com.gsm.model.ProductVariant;
import com.gsm.model.SaleOrder;
import com.gsm.model.SaleOrderDetail;
import com.gsm.repository.ProductionOutputRepository;
import com.gsm.repository.SaleOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final SaleOrderRepository saleOrderRepository;
    private final ProductionOutputRepository productionOutputRepository;

    @Autowired
    public DashboardServiceImpl(SaleOrderRepository saleOrderRepository, ProductionOutputRepository productionOutputRepository) {
        this.saleOrderRepository = saleOrderRepository;
        this.productionOutputRepository = productionOutputRepository;
    }

    @Override
    public MainDashboardDto getDashboardData() {
        MainDashboardDto mainDto = new MainDashboardDto();
        // Gọi các phương thức đã có để lấy dữ liệu cho từng tab
        mainDto.setWipTab(getWipDashboardData());
        mainDto.setPerformanceTab(getPerformanceDashboardData());
        return mainDto;
    }

    @Override
    public DashboardWIPDto getWipDashboardData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        List<SaleOrder> inProgressOrders = saleOrderRepository.findByStatus(SaleOrderStatus.InProgress);
        List<ProductionOutputRepository.ProductionOutputGroup> outputSummaries = productionOutputRepository.getAggregatedOutputForInProgressOrders();

        Map<String, Long> outputMap = outputSummaries.stream()
                .collect(Collectors.toMap(
                        summary -> summary.getSaleOrderId() + "_" + summary.getStyle() + "_" + summary.getColor() + "_" + summary.getDepartment(),
                        ProductionOutputRepository.ProductionOutputGroup::getTotalOutput,
                        Long::sum
                ));

        List<WIPDetailDto> wipDetailsList = new ArrayList<>();
        for (SaleOrder order : inProgressOrders) {
            Map<String, List<SaleOrderDetail>> detailsByProductAndColor = order.getDetails().stream()
                    .collect(Collectors.groupingBy(detail -> {
                        ProductVariant pv = detail.getProductVariant();
                        return pv.getProduct().getProductCode() + "_" + pv.getColor();
                    }));

            for (Map.Entry<String, List<SaleOrderDetail>> entry : detailsByProductAndColor.entrySet()) {
                WIPDetailDto dto = new WIPDetailDto();
                List<SaleOrderDetail> detailsGroup = entry.getValue();
                SaleOrderDetail firstDetail = detailsGroup.get(0);
                ProductVariant pv = firstDetail.getProductVariant();

                dto.setSaleOrderNo(order.getSaleOrderNo());
                dto.setStyle(pv.getProduct().getProductCode());
                dto.setColor(pv.getColor());

                int totalOrderQty = detailsGroup.stream().mapToInt(SaleOrderDetail::getOrderQuantity).sum();
                dto.setOrderQty(totalOrderQty);
                int totalShipQty = detailsGroup.stream().mapToInt(d -> d.getShipQuantity() == null ? 0 : d.getShipQuantity()).sum();
                dto.setShipQty(totalShipQty);

                String baseKeyForOutput = order.getSaleOrderId() + "_" + pv.getProduct().getProductCode() + "_" + dto.getColor();
                int cutQty = outputMap.getOrDefault(baseKeyForOutput + "_CUT", 0L).intValue();
                int sewQty = outputMap.getOrDefault(baseKeyForOutput + "_SEW", 0L).intValue();
                int pckQty = outputMap.getOrDefault(baseKeyForOutput + "_PCK", 0L).intValue();
                dto.setCutQty(cutQty);
                dto.setSewQty(sewQty);
                dto.setPckQty(pckQty);

                dto.setCutWip(cutQty - sewQty);
                dto.setSewWip(sewQty - pckQty);
                dto.setPckWip(pckQty - totalShipQty);

                dto.setRemark(generateRemark(order, dto));

                // === SỬA LỖI TẠI ĐÂY ===
                // Chỉ cần định dạng và gán, không cần parse ngược lại
                if (order.getShipDate() != null) {
                    dto.setShipDate(order.getShipDate().format(formatter));
                }
                if (order.getProductionStartDate() != null) {
                    dto.setProductionStartDate(order.getProductionStartDate().format(formatter));
                }

                wipDetailsList.add(dto);
            }
        }

        DashboardWIPDto resultDto = new DashboardWIPDto();
        resultDto.setOrdersInProgress(inProgressOrders.size());
        resultDto.setOrdersAtRisk(wipDetailsList.stream().filter(d -> d.getRemark() != null && !d.getRemark().isEmpty()).count());
        resultDto.setTotalOrderQuantity(wipDetailsList.stream().mapToLong(WIPDetailDto::getOrderQty).sum());
        resultDto.setTotalSewnQuantity(wipDetailsList.stream().mapToLong(WIPDetailDto::getSewQty).sum());

        Map<String, Integer> wipChartData = new HashMap<>();
        wipChartData.put("CUT", wipDetailsList.stream().mapToInt(WIPDetailDto::getCutWip).sum());
        wipChartData.put("SEW", wipDetailsList.stream().mapToInt(WIPDetailDto::getSewWip).sum());
        wipChartData.put("PACK", wipDetailsList.stream().mapToInt(WIPDetailDto::getPckWip).sum());
        resultDto.setWipByDepartmentChart(wipChartData);
        resultDto.setWipDetails(wipDetailsList);

        return resultDto;
    }

    private String generateRemark(SaleOrder order, WIPDetailDto dto) {
        List<String> remarks = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Ưu tiên 1: Kiểm tra các vấn đề chặn (Blockers)
        // Cảnh báo "Chưa sản xuất" là nghiêm trọng nhất
        if (order.getProductionStartDate() != null && today.isAfter(order.getProductionStartDate()) && dto.getCutQty() == 0) {
            remarks.add("Not Started");
            return String.join(", ", remarks); // Nếu chưa sản xuất, không cần kiểm tra các lỗi khác
        }

        // (Tương lai) Có thể thêm kiểm tra thiếu vật tư ở đây
        // if (order.getFabricArrivalDate() != null && order.getProductionStartDate().isBefore(order.getFabricArrivalDate())) {
        //     remarks.add("Material Delay");
        //     return String.join(", ", remarks); // Nếu thiếu NPL, đây là cảnh báo quan trọng nhất
        // }

        // Ưu tiên 2: Các vấn đề về tiến độ và hiệu suất (chỉ kiểm tra khi đã sản xuất)
        // Cảnh báo "Nguy cơ thiếu hàng" khi gần đến ngày giao
        if (order.getShipDate() != null && today.isAfter(order.getShipDate().minusDays(3)) && dto.getOrderQty() > 0) {
            if (dto.getPckQty() < dto.getOrderQty() * 0.95) {
                remarks.add("Short Qty Risk"); // Rõ nghĩa hơn: "Nguy cơ thiếu hàng"
            }
        }

        // Cảnh báo tiến độ chậm
        if (order.getProductionStartDate() != null && order.getShipDate() != null && dto.getOrderQty() > 0) {
            if (today.isAfter(order.getProductionStartDate()) && today.isBefore(order.getShipDate())) {
                long totalDays = ChronoUnit.DAYS.between(order.getProductionStartDate(), order.getShipDate());
                long daysPassed = ChronoUnit.DAYS.between(order.getProductionStartDate(), today);

                if (totalDays > 0) {
                    double timeElapsedRatio = (double) daysPassed / totalDays;
                    double sewCompleteRatio = (double) dto.getSewQty() / dto.getOrderQty();

                    if (timeElapsedRatio > 0.5 && sewCompleteRatio < (timeElapsedRatio * 0.8)) {
                        remarks.add("Behind Pace"); // Rút gọn: "Chậm tiến độ"
                    }
                }
            }
        }

        // Ưu tiên 3: Các vấn đề về tồn kho (WIP)
        // Cảnh báo "nút thắt cổ chai"
        if (dto.getCutWip() > 5000) { // Ngưỡng có thể được cấu hình
            remarks.add("Sewing Neck"); // Rút gọn: "Tắc nghẽn chuyền May"
        }

        return String.join(", ", remarks);
    }

    /**
     * =================================================================
     * PHẦN LOGIC MỚI CHO TAB "PERFORMANCE ANALYSIS"
     * =================================================================
     */
    @Override
    @Transactional(readOnly = true)
    public PerformanceTabDto getPerformanceDashboardData() {
        PerformanceTabDto performanceData = new PerformanceTabDto();
        calculateDeliveryMetrics(performanceData);
        performanceData.setDailyThroughputChart(calculateDailyThroughput());
        performanceData.setSCurveChart(calculateSCurveData());
        performanceData.setMaterialRiskTable(analyzeMaterialRisks());
        return performanceData;
    }

    private void calculateDeliveryMetrics(PerformanceTabDto dto) {
        List<SaleOrder> completedOrders = saleOrderRepository.findByStatus(SaleOrderStatus.Shipped);
        if (completedOrders.isEmpty()) {
            dto.setOnTimeCompletionRate(100.0);
            dto.setAverageLeadTime(0.0);
            return;
        }

        long onTimeCount = 0;
        long totalLeadTimeDays = 0;

        for (SaleOrder order : completedOrders) {
            if (order.getShipDate() != null && order.getLastModifiedDate() != null) {
                LocalDate completionDate = order.getLastModifiedDate().toLocalDate();
                // Nếu ngày hoàn thành không sau ngày giao hàng kế hoạch thì là đúng hạn
                if (!completionDate.isAfter(order.getShipDate())) {
                    onTimeCount++;
                }
            }
            if (order.getProductionStartDate() != null && order.getLastModifiedDate() != null) {
                totalLeadTimeDays += ChronoUnit.DAYS.between(order.getProductionStartDate(), order.getLastModifiedDate().toLocalDate());
            }
        }

        dto.setOnTimeCompletionRate((double) onTimeCount * 100.0 / completedOrders.size());
        dto.setAverageLeadTime((double) totalLeadTimeDays / completedOrders.size());
    }

    private DailyThroughputChartDto calculateDailyThroughput() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // Lấy đủ 30 ngày kể cả hôm nay

        // 1. Gọi phương thức repository mới
        List<ProductionOutputRepository.DailyThroughputByDeptResult> results =
                productionOutputRepository.findDailyThroughputByDepartment(startDate, endDate);

        // 2. Nhóm kết quả theo ngày để dễ xử lý
        Map<LocalDate, Map<String, Long>> dataByDate = results.stream()
                .collect(Collectors.groupingBy(
                        ProductionOutputRepository.DailyThroughputByDeptResult::getDate,
                        Collectors.toMap(
                                ProductionOutputRepository.DailyThroughputByDeptResult::getDepartment,
                                ProductionOutputRepository.DailyThroughputByDeptResult::getTotalQuantity,
                                Long::sum
                        )
                ));

        // 3. Chuẩn bị các danh sách để chứa dữ liệu cuối cùng
        List<String> labels = new ArrayList<>();
        List<Long> cuttingData = new ArrayList<>();
        List<Long> sewingData = new ArrayList<>();
        List<Long> packingData = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

        // 4. Lặp qua 30 ngày để đảm bảo không ngày nào bị thiếu
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            labels.add(date.format(formatter));
            Map<String, Long> dailyData = dataByDate.getOrDefault(date, Collections.emptyMap());

            cuttingData.add(dailyData.getOrDefault("CUT", 0L));
            sewingData.add(dailyData.getOrDefault("SEW", 0L));
            packingData.add(dailyData.getOrDefault("PCK", 0L));
        }

        // 5. Tạo và trả về DTO
        DailyThroughputChartDto chartDto = new DailyThroughputChartDto();
        chartDto.setLabels(labels);
        chartDto.setCuttingData(cuttingData);
        chartDto.setSewingData(sewingData);
        chartDto.setPackingData(packingData);

        return chartDto;
    }

    private SCurveChartDto calculateSCurveData() {
        SCurveChartDto sCurve = new SCurveChartDto();
        List<SaleOrder> inProgressOrders = saleOrderRepository.findByStatus(SaleOrderStatus.InProgress);
        if (inProgressOrders.isEmpty()) {
            return new SCurveChartDto(); // Trả về rỗng nếu không có đơn hàng
        }

        // 1. Xác định khung thời gian và tổng kế hoạch
        LocalDate minStartDate = inProgressOrders.stream().map(SaleOrder::getProductionStartDate).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxShipDate = inProgressOrders.stream().map(SaleOrder::getShipDate).max(LocalDate::compareTo).orElse(LocalDate.now().plusDays(1));
        long totalOrderQty = inProgressOrders.stream().flatMap(o -> o.getDetails().stream()).mapToLong(SaleOrderDetail::getOrderQuantity).sum();
        long totalDays = ChronoUnit.DAYS.between(minStartDate, maxShipDate) + 1;
        if (totalDays <= 0) return new SCurveChartDto();

        // 2. Lấy dữ liệu thực tế đã cộng dồn
        List<ProductionOutputRepository.CumulativeOutputResult> actualResults = productionOutputRepository.findCumulativeSewnOutputForInProgressOrders();
        Map<LocalDate, Long> actualMap = actualResults.stream().collect(Collectors.toMap(
                ProductionOutputRepository.CumulativeOutputResult::getDate,
                ProductionOutputRepository.CumulativeOutputResult::getCumulativeQuantity
        ));

        // 3. Tạo các điểm dữ liệu cho biểu đồ
        List<String> labels = new ArrayList<>();
        List<Long> plannedData = new ArrayList<>();
        List<Long> actualData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        long dailyPlanQty = totalOrderQty / totalDays;

        AtomicLong lastActualQty = new AtomicLong(0);

        minStartDate.datesUntil(maxShipDate.plusDays(1)).forEach(date -> {
            labels.add(date.format(formatter));
            long currentPlanned = (ChronoUnit.DAYS.between(minStartDate, date) + 1) * dailyPlanQty;
            plannedData.add(currentPlanned > totalOrderQty ? totalOrderQty : currentPlanned);

            // Lấy giá trị thực tế, nếu không có thì lấy giá trị của ngày hôm trước
            long currentActual = actualMap.getOrDefault(date, lastActualQty.get());
            actualData.add(currentActual);
            lastActualQty.set(currentActual);
        });

        sCurve.setLabels(labels);
        sCurve.setPlannedData(plannedData);
        sCurve.setActualData(actualData);
        return sCurve;
    }

    private List<MaterialRiskDto> analyzeMaterialRisks() {
        // TODO: Thay thế bằng logic gọi PurchaseOrderRepository thật
        // Dữ liệu giả để minh họa
        List<MaterialRiskDto> mockData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");

        MaterialRiskDto risk1 = new MaterialRiskDto();
        risk1.setSaleOrderNo("IVM00002");
        risk1.setStyle("ShirtsTest001");
        risk1.setProductionStartDate(LocalDate.now().plusDays(5).format(formatter));
        risk1.setMaterialDescription("Main Fabric - Cotton");
        risk1.setPurchaseOrderNo("PO-FAB-101");
        risk1.setPoArrivalDate(LocalDate.now().plusDays(7).format(formatter)); // NPL về trễ 2 ngày
        risk1.setDaysDifference(-2);
        risk1.setRiskStatus("At Risk");
        mockData.add(risk1);

        MaterialRiskDto risk2 = new MaterialRiskDto();
        risk2.setSaleOrderNo("FGL00001");
        risk2.setStyle("ShirtsTest001");
        risk2.setProductionStartDate(LocalDate.now().plusDays(10).format(formatter));
        risk2.setMaterialDescription("Buttons - White");
        risk2.setPurchaseOrderNo("PO-TRM-203");
        risk2.setPoArrivalDate(LocalDate.now().plusDays(8).format(formatter));
        risk2.setDaysDifference(2);
        risk2.setRiskStatus("On Track");
        mockData.add(risk2);

        return mockData;
    }
}