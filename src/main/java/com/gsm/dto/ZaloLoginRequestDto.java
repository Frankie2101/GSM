package com.gsm.dto;

import lombok.Data;

@Data
public class ZaloLoginRequestDto {
    private String accessToken;

    // Sửa lại để nhận token từ Zalo Mini App
    private String phoneNumberToken;
}
