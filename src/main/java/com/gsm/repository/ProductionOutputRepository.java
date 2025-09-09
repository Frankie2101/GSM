package com.gsm.repository;

import com.gsm.model.ProductionOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionOutputRepository extends JpaRepository<ProductionOutput, Long> {

    /**
     * Hàm tìm kiếm nâng cao cho chức năng Production Output List
     */
    @Query("SELECT po FROM ProductionOutput po JOIN po.saleOrder so WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR so.saleOrderNo LIKE %:keyword% OR po.style LIKE %:keyword% OR po.color LIKE %:keyword%) AND " +
            "(:department IS NULL OR :department = '' OR po.department LIKE %:department%) AND " +
            "(:productionLine IS NULL OR :productionLine = '' OR po.productionLine LIKE %:productionLine%) AND " +
            "(cast(:outputDateFrom as date) IS NULL OR po.outputDate >= :outputDateFrom) AND " +
            "(cast(:outputDateTo as date) IS NULL OR po.outputDate <= :outputDateTo)")
    List<ProductionOutput> search(
            @Param("keyword") String keyword,
            @Param("outputDateFrom") LocalDate outputDateFrom,
            @Param("outputDateTo") LocalDate outputDateTo,
            @Param("department") String department,
            @Param("productionLine") String productionLine
    );

    // --- CÁC PHƯƠMNG THỨC CHO DASHBOARD ---

    /**
     * Interface định nghĩa cấu trúc dữ liệu trả về của câu query tổng hợp cho dashboard.
     */
    interface ProductionOutputGroup {
        Long getSaleOrderId();
        String getStyle();
        String getColor();
        String getDepartment();
        Long getTotalOutput();
    }

    // Interface để nhận kết quả từ query Daily Throughput
    interface DailyThroughputResult {
        LocalDate getDate();
        Long getTotalQuantity();
    }

    // Query để lấy tổng sản lượng hoàn thành (Pack Qty) theo từng ngày
    @Query("SELECT po.outputDate as date, SUM(po.outputQuantity) as totalQuantity " +
            "FROM ProductionOutput po " +
            "WHERE po.department = 'PCK' AND po.outputDate BETWEEN :startDate AND :endDate " +
            "GROUP BY po.outputDate ORDER BY po.outputDate ASC")
    List<DailyThroughputResult> findDailyThroughput(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Interface để nhận kết quả cho S-Curve
    interface CumulativeOutputResult {
        LocalDate getDate();
        Long getCumulativeQuantity();
    }

    // Query để lấy sản lượng MAY cộng dồn theo từng ngày cho các đơn hàng InProgress
    @Query(value = "SELECT T.OutputDate AS date, SUM(T.TotalQuantity) OVER (ORDER BY T.OutputDate) AS cumulativeQuantity " +
            "FROM ( " +
            "  SELECT po.OutputDate, SUM(po.OutputQuantity) AS TotalQuantity " +
            "  FROM ProductionOutput po JOIN SaleOrder so ON po.SaleOrderId = so.SaleOrderId " +
            "  WHERE so.Status = 'InProgress' AND po.Department = 'SEW' " +
            "  GROUP BY po.OutputDate " +
            ") T ORDER BY T.OutputDate ASC", nativeQuery = true)
    List<CumulativeOutputResult> findCumulativeSewnOutputForInProgressOrders();

    /**
     * Query tính tổng sản lượng theo từng (Đơn hàng, Mã hàng, Màu, Công đoạn)
     * chỉ dành cho các đơn hàng có trạng thái "InProgress" để tối ưu hiệu năng.
     */
    @Query("SELECT " +
            "po.saleOrder.saleOrderId AS saleOrderId, " +
            "po.style AS style, " +
            "po.color AS color, " +
            "po.department AS department, " +
            "SUM(po.outputQuantity) AS totalOutput " +
            "FROM ProductionOutput po " +
            "WHERE po.saleOrder.status = com.gsm.enums.SaleOrderStatus.InProgress " +
            "GROUP BY po.saleOrder.saleOrderId, po.style, po.color, po.department")
    List<ProductionOutputGroup> getAggregatedOutputForInProgressOrders();

    public interface DailyThroughputByDeptResult {
        LocalDate getDate();
        String getDepartment();
        Long getTotalQuantity();
    }

    // Thêm phương thức truy vấn mới này
    @Query("SELECT po.outputDate AS date, po.department AS department, SUM(po.outputQuantity) AS totalQuantity " +
            "FROM ProductionOutput po " +
            "WHERE po.outputDate BETWEEN :startDate AND :endDate " +
            "GROUP BY po.outputDate, po.department " +
            "ORDER BY po.outputDate, po.department")
    List<DailyThroughputByDeptResult> findDailyThroughputByDepartment(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}