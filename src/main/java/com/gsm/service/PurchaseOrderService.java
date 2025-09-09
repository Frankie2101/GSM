package com.gsm.service;

import com.gsm.dto.OrderBOMDto;
import com.gsm.dto.PurchaseOrderDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface PurchaseOrderService {

    // Lấy danh sách tất cả PO
    List<PurchaseOrderDto> findAll();

    // Tìm một PO theo ID
    PurchaseOrderDto findById(Long id);

    // Lưu (tạo mới/cập nhật) một PO
    PurchaseOrderDto save(PurchaseOrderDto dto);

    // Xóa một PO
    void deleteById(Long id);

    // Trình duyệt PO
    void submitForApproval(Long id);

    // Tìm các PO đang chờ duyệt
    List<PurchaseOrderDto> findPendingApproval();

    // Duyệt PO
    void approve(Long id);

    // Từ chối PO
    void reject(Long id);
}