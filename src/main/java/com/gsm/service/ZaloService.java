// File: src/main/java/com/gsm/service/ZaloService.java
package com.gsm.service;

import com.gsm.dto.*;

import java.util.List;

public interface ZaloService {

    // Luồng đăng nhập mới: Dùng token SĐT để lấy thông tin User
    UserDto loginByZaloId(String zaloUserId);

    UserDto linkAccount(ZaloLinkRequestDto linkRequest);


    // [MỚI] Tìm style và color cho Mini App
    ZaloSaleOrderInfoDto findStylesAndColorsBySaleOrderNo(String saleOrderNo);

    // Lưu sản lượng từ Mini App
    void saveProductionOutputs(List<ProductionOutputDto> outputDtos);
}