package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Fabric")
@Getter
@Setter
@NoArgsConstructor
public class Fabric extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FabricId")
    private Long fabricId;

    @Column(name = "ActiveFlag", nullable = false)
    private boolean activeFlag = true;

    @Column(name = "FabricCode", nullable = false, unique = true, length = 50)
    private String fabricCode;

    @Column(name = "FabricName", nullable = false, length = 100)
    private String fabricName;

    @Column(name = "FabricType", length = 50)
    private String fabricType;

    @Column(name = "Construction", length = 100)
    private String construction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UnitId", referencedColumnName = "UnitId")
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupplierId", referencedColumnName = "SupplierId")
    private Supplier supplier;

    @Column(name = "YarnCount", length = 50)
    private String yarnCount;

    @Column(name = "FabricContent", length = 255)
    private String fabricContent;

    @Column(name = "Finishing", length = 100)
    private String finishing;

    @OneToMany(mappedBy = "fabric", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FabricColor> fabricColors = new ArrayList<>();

    // Helper method to keep relationship in sync
    public void addColor(FabricColor color) {
        this.fabricColors.add(color);
        color.setFabric(this);
    }
}
