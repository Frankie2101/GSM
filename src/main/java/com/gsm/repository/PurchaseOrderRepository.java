package com.gsm.repository;

import com.gsm.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    /**
     * Đếm số lượng PO đã được tạo ra từ một Sale Order cụ thể.
     * Query này sẽ join qua các bảng để tìm liên kết.
     */
    @Query("SELECT count(po) FROM PurchaseOrder po " +
            "JOIN po.details pod " +
            "JOIN pod.orderBOMDetail obd " +
            "JOIN obd.orderBOM ob " +
            "WHERE ob.saleOrder.saleOrderId = :saleOrderId")
    long countBySaleOrderId(@Param("saleOrderId") Long saleOrderId);

    @Query("SELECT DISTINCT po FROM PurchaseOrder po " +
            "LEFT JOIN FETCH po.details " +
            "ORDER BY po.poDate DESC, po.purchaseOrderId DESC")
    List<PurchaseOrder> findAllWithDetails();

    /**
     * Tìm PO theo ID và fetch các details liên quan trong cùng một query.
     * @param id ID của PO.
     * @return Optional chứa PurchaseOrder.
     */
    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.details WHERE po.purchaseOrderId = :id")
    Optional<PurchaseOrder> findByIdWithDetails(@Param("id") Long id);

    /**
     * Lấy tất cả PO và fetch các supplier liên quan để hiển thị danh sách hiệu quả.
     * @return Danh sách PurchaseOrder.
     */
    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier ORDER BY po.poDate DESC, po.purchaseOrderId DESC")
    List<PurchaseOrder> findAllWithSupplier();

    /**
     * THÊM MỚI: Query để tìm PO theo status, dùng cho màn hình Pending Approval.
     */
    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier WHERE po.status = :status ORDER BY po.poDate DESC")
    List<PurchaseOrder> findByStatus(@Param("status") String status);
}
