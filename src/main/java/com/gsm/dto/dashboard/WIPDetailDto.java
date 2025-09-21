package com.gsm.dto.dashboard;

import lombok.Data;

/**
 * A DTO representing a single row in the WIP details table on the dashboard.
 */
@Data
public class WIPDetailDto {
    private String saleOrderNo;
    private String style;
    private String color;
    private String shipDate;
    private String productionStartDate;
    private int orderQty;
    private int shipQty;
    private int cutQty;
    private int sewQty;
    private int pckQty;
    private int wshQty;
    private int dipQty;
    private int cutWip;
    private int sewWip;
    private int pckWip;
    private String remark;
}