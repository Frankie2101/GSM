package com.gsm.dto;

import  com.gsm.enums.UserType;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object for the User entity.
 * Used for transferring user data between the client and server, with validation rules.
 */
@Data
public class UserDto {
    private Long userId;
    private Long sequenceNumber;

    @NotBlank(message = "User Name cannot be blank")
    @Size(max = 100)
    private String userName;

    @NotBlank(message = "Department cannot be blank")
    private String department;
    private String productionLine;

    @NotBlank(message = "Phone Number cannot be blank")
    @Size(max = 20)
    private String phoneNumber;

    private UserType userType;
    private String emailAddress;
    private boolean activeFlag;

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}