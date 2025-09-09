package com.gsm.dto.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MaterialRiskDto {
    private String saleOrderNo;
    private String style;
    private String productionStartDate; // Dạng String MM/dd/yy
    private String materialDescription; // Mô tả NPL chính
    private String purchaseOrderNo;
    private String poArrivalDate; // Dạng String MM/dd/yy
    private long daysDifference; // Chênh lệch ngày, âm là rủi ro
    private String riskStatus; // "At Risk" hoặc "On Track"
}