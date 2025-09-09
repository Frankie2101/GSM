package com.gsm.service;

import com.gsm.dto.ProductionOutputDto;
import com.gsm.dto.UserDto;
import com.gsm.dto.ZaloLoginRequestDto;
import com.gsm.dto.ZaloSaleOrderDetailDto;

import java.util.List;

public interface ZaloService {

    UserDto login(ZaloLoginRequestDto loginRequest);

    List<ZaloSaleOrderDetailDto> getSaleOrderDetailsForZalo(String saleOrderNo);

    void saveProductionOutputs(List<ProductionOutputDto> outputDtos, Long userId);
}