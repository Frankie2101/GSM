package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "OrderBOM")
@Getter
@Setter
public class OrderBOM extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderBOMId;

    @OneToOne
    @JoinColumn(name = "SaleOrderId", nullable = false)
    private SaleOrder saleOrder;

    @ManyToOne
    @JoinColumn(name = "BOMTemplateId")
    private BOMTemplate bomTemplate;

    @OneToMany(mappedBy = "orderBOM", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderBOMDetail> details = new ArrayList<>();

    public void addDetail(OrderBOMDetail detail) {
        details.add(detail);
        detail.setOrderBOM(this);
    }
}