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
import com.gsm.repository.PurchaseOrderRepository;


/**
 * The concrete implementation of the DashboardService interface.
 * Contains all business logic for calculating KPIs and structuring chart data for the dashboard.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final SaleOrderRepository saleOrderRepository;
    private final ProductionOutputRepository productionOutputRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    public DashboardServiceImpl(SaleOrderRepository saleOrderRepository, ProductionOutputRepository productionOutputRepository, PurchaseOrderRepository purchaseOrderRepository) {
        this.saleOrderRepository = saleOrderRepository;
        this.productionOutputRepository = productionOutputRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    /**
     * The primary method that orchestrates calls to other methods to build the complete dashboard DTO.
     */
    @Override
    public MainDashboardDto getDashboardData() {
        MainDashboardDto mainDto = new MainDashboardDto();
        mainDto.setWipTab(getWipDashboardData());
        mainDto.setPerformanceTab(getPerformanceDashboardData());
        return mainDto;
    }

    /**
     * Gathers and calculates all data for the "Work in Progress" tab.
     * This includes KPIs, chart data, and a detailed WIP table.
     */
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
        // Loop through each in-progress order to build the detailed WIP list.
        for (SaleOrder order : inProgressOrders) {
            // Group details by product and color to create one WIP row per group.
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

                // Calculate total ordered and shipped quantities for the group.
                int totalOrderQty = detailsGroup.stream().mapToInt(SaleOrderDetail::getOrderQuantity).sum();
                dto.setOrderQty(totalOrderQty);
                int totalShipQty = detailsGroup.stream().mapToInt(d -> d.getShipQuantity() == null ? 0 : d.getShipQuantity()).sum();
                dto.setShipQty(totalShipQty);

                String baseKeyForOutput = order.getSaleOrderId() + "_" + pv.getProduct().getProductCode() + "_" + dto.getColor();
                // Use the output map to look up produced quantities for CUT, SEW, PCK.
                int cutQty = outputMap.getOrDefault(baseKeyForOutput + "_CUT", 0L).intValue();
                int sewQty = outputMap.getOrDefault(baseKeyForOutput + "_SEW", 0L).intValue();
                int pckQty = outputMap.getOrDefault(baseKeyForOutput + "_PCK", 0L).intValue();
                dto.setCutQty(cutQty);
                dto.setSewQty(sewQty);
                dto.setPckQty(pckQty);

                // Calculate the WIP (Work in Progress) quantity for each department.
                dto.setCutWip(cutQty - sewQty);
                dto.setSewWip(sewQty - pckQty);
                dto.setPckWip(pckQty - totalShipQty);

                // Generate a risk remark based on the order's status and progress.
                dto.setRemark(generateRemark(order, dto));

                if (order.getShipDate() != null) {
                    dto.setShipDate(order.getShipDate().format(formatter));
                }
                if (order.getProductionStartDate() != null) {
                    dto.setProductionStartDate(order.getProductionStartDate().format(formatter));
                }
                wipDetailsList.add(dto);
            }
        }

        // Aggregate results to calculate the final KPIs and chart data.
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

    /**
     * Generates a risk remark for a WIP item based on a prioritized set of rules.
     */
    private String generateRemark(SaleOrder order, WIPDetailDto dto) {
        List<String> remarks = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Priority 1: Check for critical blockers like "Not Started".
        if (order.getProductionStartDate() != null && today.isAfter(order.getProductionStartDate()) && dto.getCutQty() == 0) {
            remarks.add("Not Started");
            return String.join(", ", remarks); // Nếu chưa sản xuất, không cần kiểm tra các lỗi khác
        }

        // Priority 2: Check for progress issues like "Short Qty Risk" or "Behind Pace".
        if (order.getShipDate() != null && today.isAfter(order.getShipDate().minusDays(3)) && dto.getOrderQty() > 0) {
            if (dto.getPckQty() < dto.getOrderQty() * 0.95) {
                remarks.add("Short Qty Risk"); // Rõ nghĩa hơn: "Nguy cơ thiếu hàng"
            }
        }

        if (order.getProductionStartDate() != null && order.getShipDate() != null && dto.getOrderQty() > 0) {
            if (today.isAfter(order.getProductionStartDate()) && today.isBefore(order.getShipDate())) {
                long totalDays = ChronoUnit.DAYS.between(order.getProductionStartDate(), order.getShipDate());
                long daysPassed = ChronoUnit.DAYS.between(order.getProductionStartDate(), today);

                if (totalDays > 0) {
                    double timeElapsedRatio = (double) daysPassed / totalDays;
                    double sewCompleteRatio = (double) dto.getSewQty() / dto.getOrderQty();

                    if (timeElapsedRatio > 0.5 && sewCompleteRatio < (timeElapsedRatio * 0.8)) {
                        remarks.add("Behind Pace");
                    }
                }
            }
        }

        // Priority 3: Check for WIP issues like production bottlenecks.
        if (dto.getCutWip() > 5000) {
            remarks.add("Sewing Neck");
        }
        return String.join(", ", remarks);
    }

    /**
     * Gathers and calculates all data for the "Performance Analysis" tab.
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

    /**
     * Calculates "On-Time Completion Rate" and "Average Lead Time" from shipped orders.
     */
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

    /**
     * Calculates the daily production throughput for the last 7 days, broken down by department.
     */
    private DailyThroughputChartDto calculateDailyThroughput() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // Lấy đủ 30 ngày kể cả hôm nay

        // 1. Call the repository method to get grouped daily data.
        List<ProductionOutputRepository.DailyThroughputByDeptResult> results =
                productionOutputRepository.findDailyThroughputByDepartment(startDate, endDate);

        // 2. Process results into a map for easy lookup.
        Map<LocalDate, Map<String, Long>> dataByDate = results.stream()
                .collect(Collectors.groupingBy(
                        ProductionOutputRepository.DailyThroughputByDeptResult::getDate,
                        Collectors.toMap(
                                ProductionOutputRepository.DailyThroughputByDeptResult::getDepartment,
                                ProductionOutputRepository.DailyThroughputByDeptResult::getTotalQuantity,
                                Long::sum
                        )
                ));

        // 3. Iterate through the full date range to build the final chart data lists.
        List<String> labels = new ArrayList<>();
        List<Long> cuttingData = new ArrayList<>();
        List<Long> sewingData = new ArrayList<>();
        List<Long> packingData = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            labels.add(date.format(formatter));
            Map<String, Long> dailyData = dataByDate.getOrDefault(date, Collections.emptyMap());

            cuttingData.add(dailyData.getOrDefault("CUT", 0L));
            sewingData.add(dailyData.getOrDefault("SEW", 0L));
            packingData.add(dailyData.getOrDefault("PCK", 0L));
        }

        DailyThroughputChartDto chartDto = new DailyThroughputChartDto();
        chartDto.setLabels(labels);
        chartDto.setCuttingData(cuttingData);
        chartDto.setSewingData(sewingData);
        chartDto.setPackingData(packingData);

        return chartDto;
    }

    /**
     * Calculates the data for the S-Curve chart, comparing planned vs. actual cumulative production.
     */
    private SCurveChartDto calculateSCurveData() {
        SCurveChartDto sCurve = new SCurveChartDto();
        List<SaleOrder> inProgressOrders = saleOrderRepository.findByStatus(SaleOrderStatus.InProgress);
        if (inProgressOrders.isEmpty()) {
            return new SCurveChartDto();
        }

        // 1. Determine the overall time frame and total planned quantity.
        LocalDate minStartDate = inProgressOrders.stream().map(SaleOrder::getProductionStartDate).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxShipDate = inProgressOrders.stream().map(SaleOrder::getShipDate).max(LocalDate::compareTo).orElse(LocalDate.now().plusDays(1));
        long totalOrderQty = inProgressOrders.stream().flatMap(o -> o.getDetails().stream()).mapToLong(SaleOrderDetail::getOrderQuantity).sum();
        long totalDays = ChronoUnit.DAYS.between(minStartDate, maxShipDate) + 1;
        if (totalDays <= 0) return new SCurveChartDto();

        // 2. Fetch the actual cumulative production data.
        List<ProductionOutputRepository.CumulativeOutputResult> actualResults = productionOutputRepository.findCumulativeSewnOutputForInProgressOrders();
        Map<LocalDate, Long> actualMap = actualResults.stream().collect(Collectors.toMap(
                ProductionOutputRepository.CumulativeOutputResult::getDate,
                ProductionOutputRepository.CumulativeOutputResult::getCumulativeQuantity
        ));

        // 3. Create data points for each day, calculating linear planned progress and finding the corresponding actual progress.
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

            long currentActual = actualMap.getOrDefault(date, lastActualQty.get());
            actualData.add(currentActual);
            lastActualQty.set(currentActual);
        });

        sCurve.setLabels(labels);
        sCurve.setPlannedData(plannedData);
        sCurve.setActualData(actualData);
        return sCurve;
    }

    /**
     * Analyzes and identifies potential material-related risks.
     * This is achieved by comparing the planned arrival date of materials (from the Purchase Order)
     * against the planned production start date (from the Sale Order).
     * @return A list of DTOs, each representing a potential material risk.
     */
    private List<MaterialRiskDto> analyzeMaterialRisks() {
        // 1. Call the new repository method to get the pre-joined risk data.
        List<PurchaseOrderRepository.MaterialRiskProjection> riskData = purchaseOrderRepository.findMaterialRiskData();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");

        // 2. Convert the raw projection data into a complete list of DTOs.
        return riskData.stream()
                .map(item -> {
                    MaterialRiskDto dto = new MaterialRiskDto();
                    dto.setSaleOrderNo(item.getSaleOrderNo());
                    dto.setStyle(item.getStyle());
                    dto.setMaterialDescription(item.getMaterialDescription());
                    dto.setPurchaseOrderNo(item.getPurchaseOrderNo());
                    dto.setProductionStartDate(item.getProductionStartDate().format(formatter));
                    dto.setPoArrivalDate(item.getPoArrivalDate().format(formatter));

                    // 3. Calculate the difference in days and determine the risk status.
                    long daysDifference = ChronoUnit.DAYS.between(item.getPoArrivalDate(), item.getProductionStartDate());
                    dto.setDaysDifference(daysDifference);

                    if (daysDifference < 0) {
                        dto.setRiskStatus("Delayed"); // Material arrives after production starts -> Highest risk
                    } else if (daysDifference <= 3) {
                        dto.setRiskStatus("At Risk"); // Material arrives too close to production start (<= 3 days buffer) -> At risk
                    } else {
                        dto.setRiskStatus("On Track"); // On track / Safe
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
}