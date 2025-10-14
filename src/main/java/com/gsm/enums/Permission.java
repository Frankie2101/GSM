// In file: src/main/java/com/gsm/enums/Permission.java

package com.gsm.enums;

/**
 * Defines all granular permissions in the system, grouped by functional module.
 * This provides fine-grained control over user actions.
 */
public enum Permission {
    // Dashboard
    DASHBOARD_VIEW("Dashboard: View"),

    // User Management
    USER_VIEW("User: View"),
    USER_CREATE_EDIT("User: Create/Edit"),
    USER_DELETE("User: Delete"),

    // Master Data
    MASTER_DATA_VIEW("Master Data: View"),
    MASTER_DATA_EDIT("Master Data: Edit/Import"),

    // Product, Fabric, Trim (share the same permission structure)
    PRODUCT_VIEW("Product: View"),
    PRODUCT_CREATE_EDIT("Product: Create/Edit"),
    PRODUCT_DELETE("Product: Delete"),
    FABRIC_VIEW("Fabric: View"),
    FABRIC_CREATE_EDIT("Fabric: Create/Edit"),
    FABRIC_DELETE("Fabric: Delete"),
    TRIM_VIEW("Trim: View"),
    TRIM_CREATE_EDIT("Trim: Create/Edit"),
    TRIM_DELETE("Trim: Delete"),

    // BOM Template
    BOM_TEMPLATE_VIEW("BOM Template: View"),
    BOM_TEMPLATE_CREATE_EDIT("BOM Template: Create/Edit"),
    BOM_TEMPLATE_DELETE("BOM Template: Delete"),

    // Sale Order
    SALE_ORDER_VIEW("Sale Order: View"),
    SALE_ORDER_CREATE_EDIT("Sale Order: Create/Edit"),
    SALE_ORDER_DELETE("Sale Order: Delete"),
    SALE_ORDER_GENERATE_BOM("Sale Order: Generate BOM"), // Specific permission for the button

    // BOM
    BOM_VIEW("BOM: View"),
    BOM_GENERATE_PO("BOM: Generate PO"), // Specific permission for the button

    // Purchase Order
    PURCHASE_ORDER_VIEW("Purchase Order: View"),
    PURCHASE_ORDER_CREATE_EDIT("Purchase Order: Create/Edit"),
    PURCHASE_ORDER_SUBMIT("Purchase Order: Submit for Approval"),
    PURCHASE_ORDER_PRINT("Purchase Order: Print/Export"),
    PURCHASE_ORDER_APPROVE("Purchase Order: Approve/Reject"), // The 'Pending Approval' permission

    // Production Output
    PRODUCTION_OUTPUT_VIEW("Production Output: View"),
    PRODUCTION_OUTPUT_CREATE_EDIT("Production Output: Create/Edit"), // Mobile App and Web
    PRODUCTION_OUTPUT_DELETE("Production Output: Delete");

    private final String displayName;

    Permission(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}