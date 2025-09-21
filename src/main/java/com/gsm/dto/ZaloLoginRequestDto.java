// File: src/main/java/com/gsm/dto/ZaloLoginRequestDto.java
package com.gsm.dto;

import lombok.Data;

/**
 * A Data Transfer Object for the Zalo login flow. It carries the Zalo User ID
 * from the Mini App to the backend for authentication.
 */
@Data
public class ZaloLoginRequestDto {

    /**
     * The unique identifier of the Zalo user attempting to log in.
     */
    private String zaloUserId;
}