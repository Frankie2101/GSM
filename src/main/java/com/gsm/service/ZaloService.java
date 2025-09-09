package com.gsm.service;

import com.gsm.dto.*; // Đảm bảo import đủ DTO
import java.util.List;

public interface ZaloService {
    // THAY ĐỔI: Cập nhật phương thức login
    ZaloLoginResponseDto login(ZaloLoginRequestDto loginRequest);

    // MỚI: Thêm phương thức kết nối tài khoản
    UserDto linkAccount(ZaloLinkRequestDto linkRequest);

    List<ZaloSaleOrderDetailDto> getSaleOrderDetailsForZalo(String saleOrderNo);
    void saveProductionOutputs(List<ProductionOutputDto> outputDtos, Long userId);
}