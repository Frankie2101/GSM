package com.gsm.controller;

import com.gsm.dto.ProductDto;
import com.gsm.enums.ProductStatus;
import com.gsm.model.ProductCategory;
import com.gsm.model.Unit;
import com.gsm.repository.ProductCategoryRepository;
import com.gsm.repository.UnitRepository;
import com.gsm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken; // SỬA IMPORT NÀY
import javax.servlet.http.HttpServletRequest; // THÊM IMPORT NÀY
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private ProductCategoryRepository categoryRepository;
    @Autowired private UnitRepository unitRepository;

    @GetMapping("/form")
    public String showProductForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) {
        ProductDto product;
        if (id != null) {
            product = productService.findById(id);
        } else {
            product = new ProductDto();
            product.setStatus(ProductStatus.Active);
            product.setVariants(new ArrayList<>()); // Khởi tạo list rỗng cho sản phẩm mới
        }

        // 1. Xử lý cho Category Dropdown
        List<ProductCategory> allCategories = categoryRepository.findAll();
        List<Map<String, Object>> categoryOptions = new ArrayList<>();
        for (ProductCategory category : allCategories) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", category.getCategoryId());
            option.put("name", category.getCategoryName());
            // So sánh ID của category trong danh sách với ID đã lưu trong sản phẩm
            if (product.getCategoryId() != null && product.getCategoryId().equals(category.getCategoryId())) {
                option.put("selected", true);
            }
            categoryOptions.add(option);
        }

        // 2. Xử lý cho Unit Dropdown (tương tự)
        List<Unit> allUnits = unitRepository.findAll();
        List<Map<String, Object>> unitOptions = new ArrayList<>();
        for (Unit unit : allUnits) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", unit.getUnitId());
            option.put("name", unit.getUnitName());
            // So sánh ID của unit trong danh sách với ID đã lưu trong sản phẩm
            if (product.getUnitId() != null && product.getUnitId().equals(unit.getUnitId())) {
                option.put("selected", true);
            }
            unitOptions.add(option);
        }

        // --- KẾT THÚC LOGIC MỚI ---

        // Tạo danh sách status (giữ nguyên)
        List<Map<String, String>> statusList = new ArrayList<>();
        for (ProductStatus status : ProductStatus.values()) {
            Map<String, String> statusMap = new HashMap<>();
            statusMap.put("value", status.name());
            statusMap.put("displayName", status.name());
            if (status == product.getStatus()) {
                statusMap.put("selected", "true");
            }
            statusList.add(statusMap);
        }

        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));

        model.addAttribute("statuses", statusList);
        model.addAttribute("product", product);
        model.addAttribute("isProductPage", true);

        // Gửi danh sách đã được xử lý ra view
        model.addAttribute("categories", categoryOptions);
        model.addAttribute("units", unitOptions);

        return "product/product_form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute ProductDto productDto, RedirectAttributes redirectAttributes) {
        try {
            ProductDto savedProduct = productService.save(productDto);
            redirectAttributes.addFlashAttribute("successMessage", "Saved Successfully!");
            return "redirect:/products/form?id=" + savedProduct.getProductId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("product", productDto);
            return "redirect:/products/form";
        }
    }

    @GetMapping
    public String showProductList(
            // THÊM MỚI: Nhận tham số tìm kiếm từ URL
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Model model) {

        List<ProductDto> products;
        // Nếu có keyword hoặc category, thực hiện tìm kiếm
        if ((keyword != null && !keyword.isEmpty()) || (category != null && !category.isEmpty())) {
            products = productService.search(keyword, category);
        } else {
            // Nếu không, hiển thị tất cả sản phẩm
            products = productService.findAll();
        }

        model.addAttribute("products", products);
        model.addAttribute("isProductPage", true);

        // Gửi lại các giá trị đã tìm kiếm để hiển thị trên form
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);

        // Lấy danh sách category cho dropdown
        model.addAttribute("categories", categoryRepository.findAll());

        return "product/product_list";
    }

    // ... (phương thức showProductForm và saveProduct giữ nguyên) ...

    @PostMapping("/delete")
    public String deleteProducts(
            // THÊM MỚI: Nhận danh sách ID cần xóa
            @RequestParam(value = "selectedIds", required = false) List<Long> ids,
            RedirectAttributes redirectAttributes) {

        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one product to delete.");
        } else {
            productService.deleteByIds(ids);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted " + ids.size() + " product(s).");
        }

        return "redirect:/products";
    }
}
