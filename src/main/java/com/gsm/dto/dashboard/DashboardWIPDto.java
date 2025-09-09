package com.gsm.dto.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class DashboardWIPDto {
    private long ordersInProgress;
    private long ordersAtRisk;
    private long totalOrderQuantity;
    private long totalSewnQuantity;
    private Map<String, Integer> wipByDepartmentChart;
    private List<WIPDetailDto> wipDetails;
    // Xóa bỏ getter thủ công, hãy tin tưởng vào @Data của Lombok
}