package com.gsm.service;

import com.gsm.dto.dashboard.DashboardWIPDto;
import com.gsm.dto.dashboard.MainDashboardDto;
import com.gsm.dto.dashboard.PerformanceTabDto;

/**
 * Defines the contract for services that gather, process, and structure data
 * for the main application dashboard.
 */
public interface DashboardService {

    /**
     * Retrieves all data required for the "Work in Progress" (WIP) tab.
     * @return A DTO containing all WIP-related data.
     */
    DashboardWIPDto getWipDashboardData();

    /**
     * Retrieves all data required for the "Performance Analysis" tab.
     * @return A DTO containing all performance-related data.
     */
    PerformanceTabDto getPerformanceDashboardData();

    /**
     * Aggregates data from all other methods to build the complete dashboard.
     * This is the main entry point for the dashboard controller.
     * @return A DTO containing the data for the entire dashboard.
     */
    MainDashboardDto getDashboardData();
}