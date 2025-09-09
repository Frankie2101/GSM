package com.gsm.dto.dashboard; // Đảm bảo đúng package

import lombok.Data;

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