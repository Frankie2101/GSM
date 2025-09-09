package com.gsm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsm.dto.dashboard.MainDashboardDto; // Sửa import
import com.gsm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DashboardController(DashboardService dashboardService, ObjectMapper objectMapper) {
        this.dashboardService = dashboardService;
        this.objectMapper = objectMapper;
    }

    // CHỈ CÒN LẠI 1 ENDPOINT DUY NHẤT
    @GetMapping
    public String showDashboard(Model model) throws JsonProcessingException {
        MainDashboardDto dashboardData = dashboardService.getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("isDashboardPage", true);

        // Chuyển đổi dữ liệu các biểu đồ sang JSON
        if (dashboardData.getWipTab() != null) {
            model.addAttribute("wipChartJson", objectMapper.writeValueAsString(dashboardData.getWipTab().getWipByDepartmentChart()));
        }
        if (dashboardData.getPerformanceTab() != null) {
            model.addAttribute("throughputChartJson", objectMapper.writeValueAsString(dashboardData.getPerformanceTab().getDailyThroughputChart()));
            model.addAttribute("sCurveChartJson", objectMapper.writeValueAsString(dashboardData.getPerformanceTab().getSCurveChart()));
        }

        return "dashboard/dashboard"; // Trả về file view chính mới
    }
}