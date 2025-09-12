package com.gsm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZaloUserResponseDto {

    private String phone;
    private int error;
    private String message;

    @JsonProperty("data")
    private void unpackNestedPhoneNumber(Map<String, String> data) {
        if (data != null) {
            this.phone = data.get("number");
        }
    }

    // Getters and Setters
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getError() { return error; }
    public void setError(int error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "ZaloUserResponse{" +
                "phone='" + phone + '\'' +
                ", error=" + error +
                ", message='" + message + '\'' +
                '}';
    }
}