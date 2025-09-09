package com.gsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZaloLoginResponseDto {
    private com.gsm.dto.LoginStatus status;
    private UserDto user;
    private String zaloUserId;
    private String zaloUserName;
}