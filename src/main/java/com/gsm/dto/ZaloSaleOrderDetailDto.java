package com.gsm.dto;

import lombok.Data;

/**
 * A Data Transfer Object used to represent aggregated/summarized sale order detail information.
 * It groups data by style and color, likely for reporting purposes.
 */
@Data
public class ZaloSaleOrderDetailDto {

    /**
     * The identifying keys for the group.
     */
    private String style;
    private String color;

    /**
     * Aggregated information for the group.
     */
    private Integer totalOrderQty;
    private Integer totalShippedQty;
}

