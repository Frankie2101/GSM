package com.gsm.dto.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * A DTO that structures the data needed for the S-Curve chart.
 */
@Data
@NoArgsConstructor
public class SCurveChartDto {

    /**
     * Labels for the X-axis (e.g., dates).
     */
    private List<String> labels;

    /**
     * The planned cumulative production data (the target curve).
     */
    private List<Long> plannedData;

    /**
     * The actual cumulative production data (the actual progress curve).
     */
    private List<Long> actualData;
}