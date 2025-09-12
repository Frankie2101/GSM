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

@RestController
@RequestMapping("/api/sale-orders")
public class SaleOrderApiController {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository productVariantRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private SaleOrderRepository saleOrderRepository;
    @Autowired private SaleOrderDetailRepository saleOrderDetailRepository;

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

    @GetMapping("/{saleOrderNo}/details")
    public ResponseEntity<List<Map<String, String>>> getSaleOrderDetails(@PathVariable String saleOrderNo) {
        return saleOrderRepository.findBySaleOrderNo(saleOrderNo)
                .map(saleOrder -> {
                    List<Map<String, String>> details = saleOrderDetailRepository.findDistinctStylesAndColorsBySaleOrderId(saleOrder.getSaleOrderId());
                    return ResponseEntity.ok(details);
                })
                .orElse(ResponseEntity.ok(Collections.emptyList()));
    }

    // SỬA LẠI: Thêm logic chuyển đổi từ Object[] sang ColorInfo
    @GetMapping("/product-colors")
    public List<ColorInfo> getProductColors(@RequestParam Long productId) {
        List<Object[]> results = productVariantRepository.findDistinctColorsByProductId(productId);
        return results.stream()
                .map(result -> new ColorInfo((String) result[0], (String) result[1]))
                .collect(Collectors.toList());
    }

    @GetMapping("/product-sizes")
    public List<SizeInfo> getProductSizes(@RequestParam Long productId, @RequestParam String color) {
        return productVariantRepository.findByProduct_ProductIdAndColor(productId, color).stream()
                .map(v -> new SizeInfo(v.getProductVariantId(), v.getSize(), v.getPrice()))
                .collect(Collectors.toList());
    }

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

    // --- Inner classes for API response structure ---
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
