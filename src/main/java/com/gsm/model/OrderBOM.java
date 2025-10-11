package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the header of a Bill of Materials (BOM) for a specific Sale Order.
 */
@Entity
@Table(name = "OrderBOM")
@Getter
@Setter
public class OrderBOM extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderBOMId")
    private Long orderBOMId;

    /**
     * The one-to-one relationship with the SaleOrder this BOM belongs to.
     */
    @OneToOne
    @JoinColumn(name = "SaleOrderId", nullable = false)
    private SaleOrder saleOrder;

    /**
     * The many-to-one relationship with the BOMTemplate this BOM might be based on.
     */
    @ManyToOne
    @JoinColumn(name = "BOMTemplateId")
    private BOMTemplate bomTemplate;

    /**
     * The list of detail lines (materials) for this BOM.
     * cascade = CascadeType.ALL: Actions (persist, merge, remove) on this OrderBOM are cascaded to its details.
     * orphanRemoval = true: If a detail is removed from this list, it is also deleted from the database.
     */
    @OneToMany(mappedBy = "orderBOM", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderBOMDetail> details = new ArrayList<>();

    /**
     * Helper method to correctly manage the bidirectional relationship between OrderBOM and OrderBOMDetail.
     * @param detail The detail line to add.
     */
    public void addDetail(OrderBOMDetail detail) {
        details.add(detail);
        detail.setOrderBOM(this);
    }
}