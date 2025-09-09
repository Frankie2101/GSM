package com.gsm.controller.api;

import com.gsm.dto.dashboard.DashboardWIPDto;
import com.gsm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/wip-chart")
    public ResponseEntity<Map<String, Integer>> getWipChartData() {
        DashboardWIPDto wipData = dashboardService.getWipDashboardData();
        if (wipData == null || wipData.getWipByDepartmentChart() == null) {
            return ResponseEntity.ok(Map.of()); // Trả về JSON object rỗng nếu không có dữ liệu
        }
        return ResponseEntity.ok(wipData.getWipByDepartmentChart());
    }
}