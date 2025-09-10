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

    // --- ENDPOINTS MỚI VÀ ĐƯỢC CẬP NHẬT ---

    /**
     * [MỚI] Endpoint để xác thực người dùng dựa trên userName từ QR code.
     * Mini App sẽ gọi endpoint này sau khi quét QR để lấy thông tin đầy đủ của user.
     * @param userName Tên người dùng từ QR code.
     * @return Thông tin chi tiết của người dùng (UserDto).
     */
    @GetMapping("/user-info/{userName}")
    public ResponseEntity<UserDto> getUserInfoByUserName(@PathVariable String userName) {
        UserDto userDto = zaloService.findUserByUserName(userName);
        return ResponseEntity.ok(userDto);
    }

    /**
     * [CẬP NHẬT] Endpoint để lưu sản lượng.
     * Bây giờ nó không cần userId trong URL nữa, vì thông tin người dùng và trạm
     * sẽ được gửi trực tiếp trong payload.
     * @param outputDtos Danh sách các sản phẩm cần nhập sản lượng.
     * @return Trả về 200 OK nếu thành công.
     */
    @PostMapping("/output") // Bỏ {userId} khỏi URL
    public ResponseEntity<Void> saveProductionOutputs(@RequestBody List<ProductionOutputDto> outputDtos) {
        zaloService.saveProductionOutputs(outputDtos); // Gọi service đã được cập nhật
        return ResponseEntity.ok().build();
    }


    // --- CÁC ENDPOINTS CŨ GIỮ NGUYÊN ---

    @GetMapping("/sale-orders/{saleOrderNo}")
    public ResponseEntity<List<ZaloSaleOrderDetailDto>> getSaleOrderDetails(@PathVariable String saleOrderNo) {
        List<ZaloSaleOrderDetailDto> details = zaloService.getSaleOrderDetailsForZalo(saleOrderNo);
        return ResponseEntity.ok(details);
    }

    /**
     * Endpoint này có thể dùng để lấy token nếu cần, giữ lại để tham khảo.
     */
    @GetMapping("/init-token")
    public ResponseEntity<String> initToken(@RequestParam("code") String authorizationCode) {
        return ResponseEntity.ok("Token initialization triggered. Check server console for refresh token.");
    }
}
