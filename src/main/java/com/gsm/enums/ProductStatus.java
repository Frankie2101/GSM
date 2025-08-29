package com.gsm.enums;

/**
 * Enum định nghĩa các trạng thái có thể có của một sản phẩm.
 * Sử dụng Enum giúp đảm bảo an toàn kiểu dữ liệu và giới hạn các giá trị hợp lệ.
 */
public enum ProductStatus {
    Active,       // Đang hoạt động, kinh doanh
    Discontinued, // Ngừng sản xuất, bán hết hàng tồn
    Obsolete      // Lỗi thời, không còn sử dụng
}