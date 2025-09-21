package com.gsm.dto.dashboard;

import com.gsm.dto.dashboard.DashboardWIPDto;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The top-level Data Transfer Object for the main dashboard.
 * It acts as a container for the data of different tabs.
 */
@Data
@NoArgsConstructor
public class MainDashboardDto {

    /**
     * Data for the "Work in Progress" tab.
     */
    private DashboardWIPDto wipTab;

    /**
     * Data for the "Performance" tab.
     */
    private PerformanceTabDto performanceTab;
}