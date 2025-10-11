package com.gsm.repository;

import com.gsm.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link PurchaseOrder} entity.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    /**
     * Counts the number of POs that have been generated from a specific Sale Order.
     * This query joins through multiple tables to find the link.
     * @param saleOrderId The ID of the originating Sale Order.
     * @return The total count of associated POs.
     */
    @Query("SELECT count(po) FROM PurchaseOrder po " +
            "JOIN po.details pod " +
            "JOIN pod.orderBOMDetail obd " +
            "JOIN obd.orderBOM ob " +
            "WHERE ob.saleOrder.saleOrderId = :saleOrderId")
    long countBySaleOrderId(@Param("saleOrderId") Long saleOrderId);

    /**
     * Finds all Purchase Orders and eagerly fetches their detail lines in a single query.
     * The `LEFT JOIN FETCH` clause is a performance optimization that prevents the "N+1 query problem"
     * by loading the `details` collection at the same time as the `PurchaseOrder`.
     * @return A list of all Purchase Orders with their details initialized.
     */
    @Query("SELECT DISTINCT po FROM PurchaseOrder po " +
            "LEFT JOIN FETCH po.details " +
            "ORDER BY po.poDate DESC, po.purchaseOrderId DESC")
    List<PurchaseOrder> findAllWithDetails();

    /**
     * Finds a single Purchase Order by its ID and eagerly fetches its detail lines.
     * @param id The ID of the PO to find.
     * @return An Optional containing the Purchase Order with its details.
     */
    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.details WHERE po.purchaseOrderId = :id")
    Optional<PurchaseOrder> findByIdWithDetails(@Param("id") Long id);

    /**
     * Finds all Purchase Orders and eagerly fetches their associated suppliers.
     * This is efficient for list views where the supplier name is always needed.
     * @return A list of all Purchase Orders with their suppliers initialized.
     */
    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier ORDER BY po.poDate DESC, po.purchaseOrderId DESC")
    List<PurchaseOrder> findAllWithSupplier();

    /**
     * Finds all Purchase Orders with a specific status, also fetching their suppliers.
     * Used for the "Pending Approval" screen.
     * @param status The status to filter by (e.g., "Submitted").
     * @return A list of matching Purchase Orders.
     */
    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier WHERE po.status = :status ORDER BY po.poDate DESC")
    List<PurchaseOrder> findByStatus(@Param("status") String status);

    /**
     * An interface-based projection to hold the results of the material risk analysis query.
     * This efficiently fetches data from multiple related tables.
     */
    public interface MaterialRiskProjection {
        String getSaleOrderNo();
        String getStyle();
        LocalDate getProductionStartDate();
        String getMaterialDescription();
        String getPurchaseOrderNo();
        LocalDate getPoArrivalDate();
    }

    /**
     * A query to fetch data for material risk analysis, specifically for in-progress sales orders.
     * It joins from PurchaseOrderDetail back to SaleOrder to compare the planned material arrival date
     * against the planned production start date.
     */
    @Query("SELECT DISTINCT " +
            "so.saleOrderNo AS saleOrderNo, " +
            "p.productCode AS style, " +
            "so.productionStartDate AS productionStartDate, " +
            "CASE " +
            "   WHEN obd.materialType = 'FA' THEN fab.fabricName " +
            "   ELSE tr.trimName " +
            "END AS materialDescription, " +
            "po.purchaseOrderNo AS purchaseOrderNo, " +
            "po.arrivalDate AS poArrivalDate " +
            "FROM PurchaseOrderDetail pod " +
            "JOIN pod.purchaseOrder po " +
            "JOIN pod.orderBOMDetail obd " +
            "   LEFT JOIN obd.fabric fab " +
            "   LEFT JOIN obd.trim tr " +
            "JOIN obd.orderBOM ob " +
            "JOIN ob.saleOrder so " +
            "JOIN so.details sod " +
            "JOIN sod.productVariant pv " +
            "JOIN pv.product p " +
            "WHERE so.status = com.gsm.enums.SaleOrderStatus.InProgress " +
            "AND po.arrivalDate IS NOT NULL AND so.productionStartDate IS NOT NULL")
    List<MaterialRiskProjection> findMaterialRiskData();
}
