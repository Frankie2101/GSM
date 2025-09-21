package com.gsm.dto.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * A DTO that holds all data for the "Performance" tab on the dashboard.
 */
@Data
@NoArgsConstructor
public class PerformanceTabDto {

    private double onTimeCompletionRate;
    private double averageLeadTime;

    /**
     * Data for the daily throughput chart.
     */
    private DailyThroughputChartDto dailyThroughputChart;

    /**
     * Data for the S-Curve chart (plan vs. actual).
     */
    private SCurveChartDto sCurveChart;

    private List<MaterialRiskDto> materialRiskTable;
}