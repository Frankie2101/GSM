package com.gsm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString // Thêm ToString để log lỗi đẹp hơn
@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các trường không cần thiết mà Zalo có thể trả về
public class ZaloUserResponse {

    private String phone; // Trường này sẽ được điền bởi phương thức unpackNestedPhoneNumber
    private int error;
    private String message;

    /**
     * SỬA LỖI GỐC: Zalo trả về số điện thoại trong một object con tên là "data".
     * Jackson (thư viện chuyển đổi JSON) sẽ tự động gọi phương thức này khi thấy trường "data"
     * để "giải nén" và lấy ra số điện thoại.
     */
    @JsonProperty("data")
    private void unpackNestedPhoneNumber(Map<String, String> data) {
        if (data != null) {
            this.phone = data.get("number");
        }
    }
}