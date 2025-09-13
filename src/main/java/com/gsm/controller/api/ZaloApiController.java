package com.gsm.controller.api;

import com.gsm.dto.*;
import com.gsm.service.ZaloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zalo")
public class ZaloApiController {

    private final ZaloService zaloService;

    @Autowired
    public ZaloApiController(ZaloService zaloService) {
        this.zaloService = zaloService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> loginByZaloId(@RequestBody ZaloLoginRequestDto loginRequest) {
        UserDto userDto = zaloService.loginByZaloId(loginRequest.getZaloUserId());
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/link-account")
    public ResponseEntity<UserDto> linkAccount(@RequestBody ZaloLinkRequestDto linkRequest) {
        UserDto userDto = zaloService.linkAccount(linkRequest);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/sale-orders/{saleOrderNo}/styles")
    public ResponseEntity<ZaloSaleOrderInfoDto> getStylesAndColorsForSaleOrder(@PathVariable String saleOrderNo) { // <-- Sửa kiểu trả về
        ZaloSaleOrderInfoDto info = zaloService.findStylesAndColorsBySaleOrderNo(saleOrderNo);
        return ResponseEntity.ok(info);
    }

    @PostMapping("/output")
    public ResponseEntity<Void> saveProductionOutputs(@RequestBody List<ProductionOutputDto> outputDtos) {
        zaloService.saveProductionOutputs(outputDtos);
        return ResponseEntity.ok().build();
    }
}