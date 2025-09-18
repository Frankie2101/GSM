package com.gsm.controller.api;

import com.gsm.model.Customer;
import com.gsm.repository.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller that provides API endpoints for the Sale Order user interface.
 * These endpoints are called by frontend JavaScript to fetch dynamic data,
 * enabling a richer user experience on the sale order form.
 */
@RestController
@RequestMapping("/api/sale-orders")
public class SaleOrderApiController {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository productVariantRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private SaleOrderRepository saleOrderRepository;
    @Autowired private SaleOrderDetailRepository saleOrderDetailRepository;

    /**
     * API endpoint to get a list of all products for dropdowns.
     * @return A list of products, simplified into a {@code ProductInfo} structure.
     */
    @GetMapping("/products")
    public List<ProductInfo> getProducts() {
        return productRepository.findAll().stream()
                .map(p -> new ProductInfo(
                        p.getProductId(),
                        p.getProductCode(),
                        p.getProductName(),
                        p.getUnit() != null ? p.getUnit().getUnitName() : ""
                ))
                .collect(Collectors.toList());
    }

    /**
     * API endpoint to get the style and color details of an existing sale order.
     * @param saleOrderNo The unique number of the sale order.
     * @return A list of maps, each containing a style and color.
     */
    @GetMapping("/{saleOrderNo}/details")
    public ResponseEntity<List<Map<String, String>>> getSaleOrderDetails(@PathVariable String saleOrderNo) {
        return saleOrderRepository.findBySaleOrderNo(saleOrderNo)
                .map(saleOrder -> {
                    List<Map<String, String>> details = saleOrderDetailRepository.findDistinctStylesAndColorsBySaleOrderId(saleOrder.getSaleOrderId());
                    return ResponseEntity.ok(details);
                })
                .orElse(ResponseEntity.ok(Collections.emptyList()));
    }

    /**
     * API endpoint to fetch a distinct list of colors available for a given product.
     * @param productId The ID of the product.
     * @return A list of {@code ColorInfo} objects.
     */
    @GetMapping("/product-colors")
    public List<ColorInfo> getProductColors(@RequestParam Long productId) {
        List<Object[]> results = productVariantRepository.findDistinctColorsByProductId(productId);
        return results.stream()
                .map(result -> new ColorInfo((String) result[0], (String) result[1]))
                .collect(Collectors.toList());
    }

    /**
     * API endpoint to get available sizes for a specific product and color combination.
     * @param productId The ID of the product.
     * @param color The selected color.
     * @return A list of {@code SizeInfo} objects, including variant ID and price.
     */
    @GetMapping("/product-sizes")
    public List<SizeInfo> getProductSizes(@RequestParam Long productId, @RequestParam String color) {
        return productVariantRepository.findByProduct_ProductIdAndColor(productId, color).stream()
                .map(v -> new SizeInfo(v.getProductVariantId(), v.getSize(), v.getPrice()))
                .collect(Collectors.toList());
    }

    /**
     * API endpoint to generate the next sequential sale order number for a customer.
     * <p><b>Logic:</b> It counts existing orders for the customer and appends the next number
     * (e.g., CUSTOMER-CODE-00001).
     * @param customerCode The code of the selected customer.
     * @return A string representing the suggested next sale order number.
     */
    @GetMapping("/next-order-no")
    public String getNextOrderNo(@RequestParam String customerCode) {
        return customerRepository.findByCustomerCode(customerCode)
                .map(customer -> {
                    long count = saleOrderRepository.countByCustomer(customer);
                    String sequence = String.format("%05d", count + 1);
                    return customer.getCustomerCode() + sequence;
                })
                .orElse(customerCode + "00001"); // Fallback
    }

    // --- Inner classes for structuring API JSON responses ---
    @Getter @AllArgsConstructor public static class ProductInfo {
        private Long id; private String code; private String name; private String unitName;
    }
    @Getter @AllArgsConstructor public static class ColorInfo {
        private String colorCode; private String colorName;
    }
    @Getter @AllArgsConstructor public static class SizeInfo {
        private Long variantId; private String size; private Double price;
    }
}
