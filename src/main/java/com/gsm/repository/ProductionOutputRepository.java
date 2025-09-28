package com.gsm.repository;

import com.gsm.model.ProductionOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface ProductionOutputRepository extends JpaRepository<ProductionOutput, Long> {

    /**
     * Performs an advanced search for production outputs based on multiple criteria.
     * Searches by keyword across SO number, style, and color. Also filters by department, production line, and a date range.
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

    // --- DASHBOARD-RELATED METHODS ---

    /**
     * Projection interface for aggregated production output data.
     * Defines the structure for query results that group outputs by order, style, color, and department.
     */
    interface ProductionOutputGroup {
        Long getSaleOrderId();
        String getStyle();
        String getColor();
        String getDepartment();
        Long getTotalOutput();
    }

    /**
     * Projection for daily throughput results.
     */
    interface DailyThroughputResult {
        LocalDate getDate();
        Long getTotalQuantity();
    }

    /**
     * Calculates the total packing output quantity for each day within a given date range.
     * Used to track daily factory throughput.
     */
    @Query("SELECT po.outputDate as date, SUM(po.outputQuantity) as totalQuantity " +
            "FROM ProductionOutput po " +
            "WHERE po.department = 'PCK' AND po.outputDate BETWEEN :startDate AND :endDate " +
            "GROUP BY po.outputDate ORDER BY po.outputDate ASC")
    List<DailyThroughputResult> findDailyThroughput(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Projection for cumulative output results, typically used for S-Curve charts.
     */
    interface CumulativeOutputResult {
        LocalDate getDate();
        Long getCumulativeQuantity();
    }

    /**
     * Calculates the cumulative daily sewing output for all 'InProgress' sale orders.
     * This native query is optimized for building S-curve charts.
     */
    @Query(value = "SELECT T.OutputDate AS date, SUM(T.TotalQuantity) OVER (ORDER BY T.OutputDate) AS cumulativeQuantity " +
            "FROM ( " +
            "  SELECT po.OutputDate, SUM(po.OutputQuantity) AS TotalQuantity " +
            "  FROM ProductionOutput po JOIN SaleOrder so ON po.SaleOrderId = so.SaleOrderId " +
            "  WHERE so.Status = 'InProgress' AND po.Department = 'SEW' " +
            "  GROUP BY po.OutputDate " +
            ") T ORDER BY T.OutputDate ASC", nativeQuery = true)
    List<CumulativeOutputResult> findCumulativeSewnOutputForInProgressOrders();

    /**
     * Aggregates the total production output for each combination of Sale Order, Style, Color, and Department.
     * This query is optimized to only include orders with the 'InProgress' status to improve performance for dashboards.
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

    /**
     * Projection interface for daily throughput results grouped by department.
     * Defines the return type for queries that aggregate total daily output for each department.
     */
    public interface DailyThroughputByDeptResult {
        LocalDate getDate();
        String getDepartment();
        Long getTotalQuantity();
    }

    /**
     * Calculates the total daily production output for each department within a given date range.
     * Useful for creating detailed throughput charts broken down by department.
     */
    @Query("SELECT po.outputDate AS date, po.department AS department, SUM(po.outputQuantity) AS totalQuantity " +
            "FROM ProductionOutput po " +
            "WHERE po.outputDate BETWEEN :startDate AND :endDate " +
            "GROUP BY po.outputDate, po.department " +
            "ORDER BY po.outputDate, po.department")
    List<DailyThroughputByDeptResult> findDailyThroughputByDepartment(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Checks if any ProductionOutput record is linked to a specific SaleOrder ID.
     * This is an optimized query to quickly check for usage before allowing a delete operation.
     *
     * @param saleOrderId The ID of the SaleOrder to check.
     * @return true if the SaleOrder is in use, false otherwise.
     */
    boolean existsBySaleOrder_SaleOrderId(Long saleOrderId);

    /**
     * Finds a distinct set of all SaleOrder IDs that are currently referenced in the ProductionOutput table.
     * This is used to efficiently determine the lock status for a list of sale orders,
     * preventing the N+1 query problem.
     *
     * @return A Set containing the unique IDs of all used SaleOrders.
     */
    @Query("SELECT DISTINCT po.saleOrder.saleOrderId FROM ProductionOutput po")
    Set<Long> findDistinctSaleOrderIdsInUse();

}