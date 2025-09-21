package com.gsm.model;

import com.gsm.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

/**
 * Represents a user of the application.
 */
@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Long userId;

    @Column(name = "UserName", nullable = false, unique = true, length = 100, columnDefinition = "NVARCHAR(100)")
    private String userName;

    @Column(name = "Department", length = 100, columnDefinition = "NVARCHAR(100)")
    private String department;

    @Column(name = "ProductionLine", length = 100, columnDefinition = "NVARCHAR(100)")
    private String productionLine;

    @Column(name = "PhoneNumber", nullable = false, unique = true, length = 20, columnDefinition = "NVARCHAR(20)")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "UserType", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private UserType userType;

    @Column(name = "EmailAddress", length = 100, columnDefinition = "NVARCHAR(100)")
    private String emailAddress;

    @Column(name = "ActiveFlag", nullable = false)
    private boolean activeFlag = true;

    /**
     * The user's hashed password.
     */
    @Column(name = "Password", nullable = false)
    private String password;

    /**
     * The unique ID provided by Zalo, used for linking accounts.
     */
    @Column(name = "ZaloUserId", unique = true)
    private String zaloUserId;
}