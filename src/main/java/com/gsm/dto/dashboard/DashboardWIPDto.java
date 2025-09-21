package com.gsm.dto.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * A DTO that holds all data for the "Work in Progress" (WIP) tab on the dashboard.
 */
@Data
@NoArgsConstructor
public class DashboardWIPDto {

    /**
     * Key Performance Indicators (KPIs).
     */
    private long ordersInProgress;
    private long ordersAtRisk;
    private long totalOrderQuantity;
    private long totalSewnQuantity;

    /**
     * Data for the "WIP by Department" chart. Maps department names to quantities.
     */
    private Map<String, Integer> wipByDepartmentChart;

    /**
     * Detailed information for each order in progress, to be displayed in a table.
     */
    private List<WIPDetailDto> wipDetails;
}