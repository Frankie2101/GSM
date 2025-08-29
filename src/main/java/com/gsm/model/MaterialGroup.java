package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "MaterialGroup")
@Getter
@Setter
@NoArgsConstructor
public class MaterialGroup extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaterialGroupId")
    private Long materialGroupId;

    @Column(name = "MaterialGroupCode", nullable = false, unique = true, length = 50)
    private String materialGroupCode;

    @Column(name = "MaterialGroupName", nullable = false, length = 100)
    private String materialGroupName;

    /**
     * Loại Nguyên phụ liệu.
     * FA = Fabric (Vải)
     * TR = Trim (Phụ liệu)
     */
    @Column(name = "MaterialType", nullable = false, length = 2)
    private String materialType;
}