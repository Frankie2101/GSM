package com.gsm.dto;

import lombok.Data;

/**
 * A Data Transfer Object used to carry the necessary data for linking
 * a Zalo account to an existing system user account.
 */
@Data
public class ZaloLinkRequestDto {

    /**
     * The unique identifier of the Zalo user.
     */
    private String zaloUserId;

    /**
     * The system username that the Zalo account will be linked to.
     */
    private String userName;

    /**
     * The password for the system username, used for verification.
     */
    private String password;
}
