package com.gsm.dto.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class PerformanceTabDto {
    // KPI: Tỷ lệ hoàn thành đúng hạn
    private double onTimeCompletionRate;

    // KPI: Thời gian sản xuất trung bình (ngày)
    private double averageLeadTime;

    // Dữ liệu cho biểu đồ năng suất theo ngày (Daily Throughput)
    // Key: Ngày (String), Value: Số lượng (Integer)
    private DailyThroughputChartDto dailyThroughputChart;

    // Dữ liệu cho biểu đồ S-Curve
    private SCurveChartDto sCurveChart;

    // Dữ liệu cho bảng rủi ro vật tư
    private List<MaterialRiskDto> materialRiskTable;
}