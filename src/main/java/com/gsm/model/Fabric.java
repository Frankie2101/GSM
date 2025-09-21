package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.gsm.model.MaterialGroup;

/**
 * Represents the core Fabric entity.
 * This class maps to the "Fabric" table in the database and holds all
 * master data for a specific type of fabric.
 */
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

    /**
     * A flag to indicate if the fabric is currently in use.
     */
    @Column(name = "ActiveFlag", nullable = false)
    private boolean activeFlag = true;

    /**
     * The unique business code for the fabric.
     */
    @Column(name = "FabricCode", nullable = false, unique = true, length = 50)
    private String fabricCode;

    @Column(name = "FabricName", nullable = false, length = 100)
    private String fabricName;

    @Column(name = "FabricType", length = 50)
    private String fabricType;

    @Column(name = "Construction", length = 100)
    private String construction;

    /**
     * The associated Unit of Measure for this fabric. Loaded lazily for performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UnitId", referencedColumnName = "UnitId")
    private Unit unit;

    /**
     * The default Supplier for this fabric. Loaded lazily for performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupplierId", referencedColumnName = "SupplierId")
    private Supplier supplier;

    @Column(name = "YarnCount", length = 50)
    private String yarnCount;

    @Column(name = "FabricContent", length = 255)
    private String fabricContent;

    @Column(name = "Finishing", length = 100)
    private String finishing;

    /**
     * A list of all available colors for this fabric.
     * `orphanRemoval=true` ensures that when a color is removed from this list,
     * it is also deleted from the database.
     */
    @OneToMany(mappedBy = "fabric", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FabricColor> fabricColors = new ArrayList<>();

    /**
     * A Material group of Fabric
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaterialGroupId", referencedColumnName = "MaterialGroupId")
    private MaterialGroup materialGroup;

    /**
     * Helper method to synchronize the bidirectional relationship with FabricColor.
     * @param color The FabricColor child entity to add.
     */
    public void addColor(FabricColor color) {
        this.fabricColors.add(color);
        color.setFabric(this);
    }
}
