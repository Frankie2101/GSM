package com.gsm.controller.api;

import com.gsm.dto.dashboard.DashboardWIPDto;
import com.gsm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * A REST controller for serving dashboard data via API endpoints.
 * This can be used for asynchronous updates or by other clients.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * API endpoint to get the data for the Work-in-Progress (WIP) by Department chart.
     * @return A ResponseEntity containing a map of department names to WIP quantities.
     */
    @GetMapping("/wip-chart")
    public ResponseEntity<Map<String, Integer>> getWipChartData() {
        DashboardWIPDto wipData = dashboardService.getWipDashboardData();
        if (wipData == null || wipData.getWipByDepartmentChart() == null) {
            return ResponseEntity.ok(Map.of()); // Return an empty JSON object if no data
        }
        return ResponseEntity.ok(wipData.getWipByDepartmentChart());
    }
}