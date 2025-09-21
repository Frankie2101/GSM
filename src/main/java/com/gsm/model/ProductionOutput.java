package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Represents a single production output record.
 * This entity stores the actual quantity produced for a specific style/color
 * in a department/line on a given day.
 */
@Entity
@Table(name = "ProductionOutput")
@Getter
@Setter
@NoArgsConstructor
public class ProductionOutput extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductionOutputId")
    private Long productionOutputId;

    /**
     * The parent SaleOrder to which this production output belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SaleOrderId", nullable = false)
    private SaleOrder saleOrder;

    /**
     * The style (product name) of the item produced. Denormalized for reporting.
     */
    @Column(name = "Style", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String style;

    /**
     * The color of the item produced. Denormalized for reporting.
     */
    @Column(name = "Color", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String color;

    /**
     * The department where the output was recorded (e.g., "Cutting", "Sewing").
     */
    @Column(name = "Department", columnDefinition = "NVARCHAR(100)")
    private String department;

    /**
     * The specific production line where the output was recorded.
     */
    @Column(name = "ProductionLine", columnDefinition = "NVARCHAR(100)")
    private String productionLine;

    /**
     * The date on which this output was produced.
     */
    @Column(name = "OutputDate", nullable = false)
    private LocalDate outputDate;

    /**
     * The quantity of items produced on this date.
     */
    @Column(name = "OutputQuantity", nullable = false)
    private Integer outputQuantity;
}

