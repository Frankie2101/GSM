package com.gsm.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Unit")
@Getter
@Setter
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long unitId;

    @Column(name = "UnitCode", nullable = false, unique = true, length = 10)
    private String unitCode;

    @Column(name = "UnitName", length = 50)
    private String unitName;
}