package com.gsm.dto.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MaterialRiskDto {
    private String saleOrderNo;
    private String style;
    private String productionStartDate;
    private String materialDescription;
    private String purchaseOrderNo;
    private String poArrivalDate;
    private long daysDifference;
    private String riskStatus;
}