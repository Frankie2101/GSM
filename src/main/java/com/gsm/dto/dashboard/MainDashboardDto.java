package com.gsm.dto.dashboard;

import com.gsm.dto.dashboard.DashboardWIPDto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MainDashboardDto {
    private DashboardWIPDto wipTab;
    private PerformanceTabDto performanceTab;
}