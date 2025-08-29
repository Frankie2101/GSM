package com.gsm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception được ném ra khi cố gắng tạo một tài nguyên đã tồn tại (ví dụ: trùng mã).
 * Sẽ trả về HTTP Status 409 (Conflict).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}