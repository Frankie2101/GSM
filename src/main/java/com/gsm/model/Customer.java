// File: src/main/java/com/gsm/model/Customer.java
package com.gsm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

/**
 * Represents a customer entity in the database.
 * Each instance corresponds to a row in the "Customer" table.
 */
@Entity
@Table(name = "Customer")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends AuditableEntity {

    /**
     * The unique identifier for the customer (Primary Key).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Long customerId;

    /**
     * The business code for the customer, must be unique.
     */
    @Column(name = "CustomerCode", nullable = false, unique = true, length = 50)
    private String customerCode;

    /**
     * The full name of the customer.
     */
    @Column(name = "CustomerName", nullable = false, length = 100)
    private String customerName;

    /**
     * The physical address of the customer.
     */
    @Column(name = "Address", length = 255)
    private String address;

    /**
     * The contact phone number for the customer.
     */
    @Column(name = "ContactPhone", length = 20)
    private String contactPhone;

    /**
     * The contact email address for the customer.
     */
    @Column(name = "ContactEmail", length = 100)
    private String contactEmail;

    /**
     * Business delivery terms.
     */
    @Column(name = "DeliveryTerm", length = 100)
    private String deliveryTerm;

    /**
     * Business payment terms.
     */
    @Column(name = "PaymentTerm", length = 100)
    private String paymentTerm;

    /**
     * The default currency code used for transactions (e.g., "USD").
     */
    @Column(name = "CurrencyCode", length = 3)
    private String currencyCode;

    /**
     * The country code of the customer (e.g., "VN").
     */
    @Column(name = "CountryCode", length = 10)
    private String countryCode;
}
