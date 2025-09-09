package com.gsm.dto.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class SCurveChartDto {
    // Chứa các nhãn ngày tháng cho trục X
    private List<String> labels;
    // Chứa dữ liệu sản lượng cộng dồn theo kế hoạch
    private List<Long> plannedData;
    // Chứa dữ liệu sản lượng cộng dồn thực tế
    private List<Long> actualData;
}