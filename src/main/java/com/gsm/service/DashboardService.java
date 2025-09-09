package com.gsm.service;

import com.gsm.dto.dashboard.DashboardWIPDto; // Sửa import
import com.gsm.dto.dashboard.MainDashboardDto;
import com.gsm.dto.dashboard.PerformanceTabDto; // Thêm import này

public interface DashboardService {
    // Sửa kiểu trả về của phương thức
    DashboardWIPDto getWipDashboardData();
    PerformanceTabDto getPerformanceDashboardData();
    MainDashboardDto getDashboardData();
}