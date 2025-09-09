package com.gsm.dto.dashboard;

import lombok.Data; // QUAN TRỌNG: Thêm import này
import java.util.List;

@Data // CHỈ CẦN THÊM ANNOTATION NÀY
public class DailyThroughputChartDto {
    private List<String> labels;
    private List<Long> cuttingData;
    private List<Long> sewingData;
    private List<Long> packingData;

    // Không cần viết thêm bất kỳ hàm getter/setter nào ở đây
}