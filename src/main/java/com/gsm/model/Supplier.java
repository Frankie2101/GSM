package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

/**
 * Represents a supplier of raw materials.
 */
@Entity
@Table(name = "Supplier")
@Getter
@Setter
@NoArgsConstructor
public class Supplier extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SupplierId")
    private Long supplierId;

    @Column(name = "SupplierCode", nullable = false, unique = true, length = 50)
    private String supplierCode;

    @Column(name = "SupplierName", nullable = false, unique = true, length = 100)
    private String supplierName;

    @Column(name = "Address", length = 255)
    private String address;

    @Column(name = "ContactPhone", length = 20)
    private String contactPhone;

    @Column(name = "ContactEmail", length = 100)
    private String contactEmail;

    @Column(name = "DeliveryTerm", length = 100)
    private String deliveryTerm;

    @Column(name = "PaymentTerm", length = 100)
    private String paymentTerm;

    @Column(name = "CurrencyCode", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "TaxRate")
    private Double taxRate;

    @Column(name = "CountryCode", length = 10)
    private String countryCode;
}
