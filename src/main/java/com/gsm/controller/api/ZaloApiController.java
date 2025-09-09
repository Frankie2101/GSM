package com.gsm.controller.api;

import com.gsm.dto.ProductionOutputDto;
import com.gsm.dto.UserDto;
import com.gsm.dto.ZaloLoginRequestDto;
import com.gsm.dto.ZaloSaleOrderDetailDto;
import com.gsm.service.ZaloService;
import com.gsm.service.ZaloServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity; // Đảm bảo có import này
import org.springframework.web.bind.annotation.GetMapping; // Thêm import
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/zalo")
// Không cần @CrossOrigin ở đây nữa vì nó có thể gây xung đột với Spring Security
public class ZaloApiController {

    private final ZaloService zaloService;

    @Autowired
    public ZaloApiController(ZaloService zaloService) {
        this.zaloService = zaloService;
    }

    @GetMapping("/init-token")
    public ResponseEntity<String> initToken(@RequestParam("code") String authorizationCode) {
        // ép kiểu zaloService để gọi được hàm getInitialTokens
        ((ZaloServiceImpl) zaloService).getInitialTokens(authorizationCode);
        return ResponseEntity.ok("Token initialization triggered. Check server console for refresh token.");
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody ZaloLoginRequestDto loginRequest) {
        UserDto userDto = zaloService.login(loginRequest);
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

