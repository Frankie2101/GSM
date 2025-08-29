package com.gsm.controller.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;

/**
 * Lớp này cung cấp các thuộc tính và xử lý chung cho tất cả các Controller.
 * Ví dụ: Tự động thêm thông tin người dùng đã đăng nhập vào Model cho mọi request.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Phương thức này sẽ được tự động gọi cho mọi request.
     * Giá trị nó trả về sẽ được thêm vào Model với tên là "username".
     * @return Tên người dùng để hiển thị trên layout.
     */
    @ModelAttribute("username")
    public String getUsername() {
        // GIAI ĐOẠN HIỆN TẠI: Vì chưa có chức năng đăng nhập hoàn chỉnh,
        // chúng ta sẽ tạm thời trả về một tên người dùng cố định.
        return "ThanhDX";

        /*
        // CODE THỰC TẾ KHI CÓ SPRING SECURITY:
        // Đoạn code này sẽ được bật lên khi chúng ta làm chức năng đăng nhập.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else {
                return principal.toString();
            }
        }
        return null; // Không có ai đăng nhập
        */
    }
}