// File: src/main/java/com/gsm/dto/ZaloLoginRequestDto.java
package com.gsm.dto;

import lombok.Data;

@Data // <-- Thêm annotation này
public class ZaloLoginRequestDto {
    private String zaloUserId;

    // Lưu ý: Trong luồng đăng nhập bằng zaloUserId mới,
    // trường phoneNumberToken không còn cần thiết nữa.
    // Bạn có thể xóa nó đi để code gọn gàng hơn.
    // private String phoneNumberToken;
}