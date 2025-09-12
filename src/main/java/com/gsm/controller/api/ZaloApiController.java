// File: src/main/java/com/gsm/controller/api/ZaloApiController.java
package com.gsm.controller.api;

import com.gsm.dto.ProductionOutputDto;
import com.gsm.dto.UserDto;
import com.gsm.dto.ZaloLoginRequestDto;
import com.gsm.dto.ZaloStyleColorDto;
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

    /**
     * [API 01 - CẬP NHẬT] Endpoint đăng nhập và lấy thông tin User.
     * Mini App sẽ gửi phoneNumberToken lấy từ Zalo SDK lên đây.
     * Backend sẽ dùng token này để lấy SĐT, tìm user trong DB và trả về thông tin.
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> loginWithZalo(@RequestBody ZaloLoginRequestDto loginRequest) {
        UserDto userDto = zaloService.login(loginRequest);
        return ResponseEntity.ok(userDto);
    }

    /**
     * [API 02 - MỚI] Endpoint để lấy danh sách Style và Color dựa trên Sale Order No.
     * Mini App gọi API này sau khi người dùng nhập xong mã đơn hàng.
     */
    @GetMapping("/sale-orders/{saleOrderNo}/styles")
    public ResponseEntity<List<ZaloStyleColorDto>> getStylesAndColorsForSaleOrder(@PathVariable String saleOrderNo) {
        List<ZaloStyleColorDto> styles = zaloService.findStylesAndColorsBySaleOrderNo(saleOrderNo);
        return ResponseEntity.ok(styles);
    }

    /**
     * [API 03 - CẬP NHẬT] Endpoint để lưu sản lượng.
     * Logic đã được cập nhật trong ZaloServiceImpl để đọc userId từ payload.
     */
    @PostMapping("/output")
    public ResponseEntity<Void> saveProductionOutputs(@RequestBody List<ProductionOutputDto> outputDtos) {
        zaloService.saveProductionOutputs(outputDtos);
        return ResponseEntity.ok().build();
    }
}