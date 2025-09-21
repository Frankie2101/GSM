package com.gsm.dto.dashboard;

import lombok.Data;
import java.util.List;

/**
 * A DTO that structures the data needed for the daily throughput bar chart.
 */
@Data
public class DailyThroughputChartDto {

    /**
     * Labels for the X-axis (e.g., dates).
     */
    private List<String> labels;

    /**
     * Data series for the chart's bars.
     */
    private List<Long> cuttingData;
    private List<Long> sewingData;
    private List<Long> packingData;
}