package com.gsm.exception;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lớp xử lý exception tập trung cho toàn bộ ứng dụng.
 * Bất kỳ exception nào được ném ra từ các Controller sẽ được "bắt" tại đây.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value()); // 404
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest)request).getRequest().getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Bắt và xử lý các lỗi DuplicateResourceException (HTTP 409).
     * Thay vì hiển thị trang lỗi, chúng ta sẽ chuyển hướng về form và hiển thị thông báo.
     * @param ex Exception được ném ra.
     * @param request Dùng để lấy URL của trang gây ra lỗi.
     * @param redirectAttributes Dùng để truyền thông báo lỗi qua một lần redirect.
     * @return Lệnh chuyển hướng.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // Gửi thông báo lỗi về trang form
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        // Lấy URL của trang trước đó và chuyển hướng về đó
        // Ví dụ: nếu lỗi xảy ra khi lưu product, sẽ redirect về /products/form
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    /**
     * Bắt tất cả các lỗi còn lại (fallback) để tránh ứng dụng bị sập (HTTP 500).
     * @param ex Exception được ném ra.
     * @param model Dùng để truyền thông tin lỗi sang cho trang view.
     * @return Tên của trang view lỗi 500.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        ex.printStackTrace(); // In stack trace ra console để dev gỡ lỗi
        model.addAttribute("errorMessage", "An unexpected error occurred. Please contact support.");
        model.addAttribute("errorStatus", "500 Internal Server Error");
        return "error/error_page";
    }
}