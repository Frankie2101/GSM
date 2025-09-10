package com.gsm.service;

import com.gsm.dto.*;
import java.util.List;

public interface ZaloService {

    // [MỚI] Tìm user bằng userName
    UserDto findUserByUserName(String userName);

    // Lấy chi tiết đơn hàng
    List<ZaloSaleOrderDetailDto> getSaleOrderDetailsForZalo(String saleOrderNo);

    // [CẬP NHẬT] Lưu sản lượng, không cần userId
    void saveProductionOutputs(List<ProductionOutputDto> outputDtos);
}
