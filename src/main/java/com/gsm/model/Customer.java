// File: src/main/java/com/gsm/model/Customer.java
package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "Customer")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Long customerId;

    @Column(name = "CustomerCode", nullable = false, unique = true, length = 50)
    private String customerCode;

    @Column(name = "CustomerName", nullable = false, length = 100)
    private String customerName;

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

    @Column(name = "CurrencyCode", length = 3)
    private String currencyCode;

    @Column(name = "CountryCode", length = 10)
    private String countryCode;
}
