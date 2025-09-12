// File: src/main/java/com/gsm/service/ZaloService.java
package com.gsm.service;

import com.gsm.dto.ProductionOutputDto;
import com.gsm.dto.UserDto;
import com.gsm.dto.ZaloLoginRequestDto;
import com.gsm.dto.ZaloStyleColorDto;

import java.util.List;

public interface ZaloService {

    // Luồng đăng nhập mới: Dùng token SĐT để lấy thông tin User
    UserDto login(ZaloLoginRequestDto loginRequest);

    // [MỚI] Tìm style và color cho Mini App
    List<ZaloStyleColorDto> findStylesAndColorsBySaleOrderNo(String saleOrderNo);

    // Lưu sản lượng từ Mini App
    void saveProductionOutputs(List<ProductionOutputDto> outputDtos);
}