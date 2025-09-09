package com.gsm.controller.api;

import com.gsm.dto.*;
import com.gsm.service.ZaloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/zalo")
public class ZaloApiController {

    private final ZaloService zaloService;

    @Autowired
    public ZaloApiController(ZaloService zaloService) {
        this.zaloService = zaloService;
    }

    // THAY ĐỔI: Sửa lại endpoint để gọi đúng service
    @GetMapping("/init-token")
    public ResponseEntity<String> initToken(@RequestParam("code") String authorizationCode) {
        return ResponseEntity.ok("Token initialization triggered. Check server console for refresh token.");
    }

    @PostMapping("/login")
    public ResponseEntity<ZaloLoginResponseDto> login(@RequestBody ZaloLoginRequestDto loginRequest) {
        ZaloLoginResponseDto response = zaloService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/link-account")
    public ResponseEntity<UserDto> linkAccount(@RequestBody ZaloLinkRequestDto linkRequest) {
        UserDto userDto = zaloService.linkAccount(linkRequest);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/sale-orders/{saleOrderNo}")
    public ResponseEntity<List<ZaloSaleOrderDetailDto>> getSaleOrderDetails(@PathVariable String saleOrderNo) {
        List<ZaloSaleOrderDetailDto> details = zaloService.getSaleOrderDetailsForZalo(saleOrderNo);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/outputs/{userId}")
    public ResponseEntity<Void> saveProductionOutputs(@RequestBody List<ProductionOutputDto> outputDtos, @PathVariable Long userId) {
        zaloService.saveProductionOutputs(outputDtos, userId);
        return ResponseEntity.ok().build();
    }

}
