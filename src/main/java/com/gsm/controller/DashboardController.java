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

/**
 * A standard Spring Controller responsible for rendering the main dashboard page.
 */
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

    /**
     * Prepares and serves the main dashboard view.
     * It fetches all necessary dashboard data from the service, adds it to the model,
     * and serializes chart data into JSON strings for frontend consumption.
     * @param model The Spring Model to pass data to the view.
     * @return The name of the dashboard view template.
     * @throws JsonProcessingException If there is an error serializing data to JSON.
     */
    @GetMapping
    public String showDashboard(Model model) throws JsonProcessingException {
        MainDashboardDto dashboardData = dashboardService.getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("isDashboardPage", true);

        // Serialize chart data to JSON strings for use with a JavaScript charting library.
        if (dashboardData.getWipTab() != null) {
            model.addAttribute("wipChartJson", objectMapper.writeValueAsString(dashboardData.getWipTab().getWipByDepartmentChart()));
        }
        if (dashboardData.getPerformanceTab() != null) {
            model.addAttribute("throughputChartJson", objectMapper.writeValueAsString(dashboardData.getPerformanceTab().getDailyThroughputChart()));
            model.addAttribute("sCurveChartJson", objectMapper.writeValueAsString(dashboardData.getPerformanceTab().getSCurveChart()));
        }

        return "dashboard/dashboard";
    }
}