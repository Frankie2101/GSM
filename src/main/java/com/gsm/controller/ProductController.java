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

/**
 * Controller for handling all user-facing HTTP requests related to the Product management feature.
 * <p>
 * This includes displaying product lists and forms, as well as processing
 * form submissions for creating, updating, and deleting products.
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private ProductCategoryRepository categoryRepository;
    @Autowired private UnitRepository unitRepository;

    /**
     * Displays the product form for both creating a new product and editing an existing one.
     * <p><b>Use Case:</b> Navigated to when the user clicks the "Create New" button or the "Edit"
     * link for an existing product.
     *
     * @param id The ID of the product to edit. This is optional and will be null for creating a new product.
     * @param model The Spring Model object to pass data to the view.
     * @param request The HttpServletRequest, used here to retrieve the CSRF token.
     * @return The path to the product form view template.
     */
    @GetMapping("/form")
    public String showProductForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) {
        ProductDto product;
        if (id != null) {
            // Edit mode: fetch existing product data.
            product = productService.findById(id);
        } else {
            // Create mode: prepare a new, empty DTO with default values.
            product = new ProductDto();
            product.setStatus(ProductStatus.Active);
            product.setVariants(new ArrayList<>());
        }

        // Prepare data for dropdowns (Categories, Units, Statuses)
        List<ProductCategory> allCategories = categoryRepository.findAll();
        List<Map<String, Object>> categoryOptions = new ArrayList<>();
        for (ProductCategory category : allCategories) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", category.getCategoryId());
            option.put("name", category.getCategoryName());
            if (product.getCategoryId() != null && product.getCategoryId().equals(category.getCategoryId())) {
                option.put("selected", true);
            }
            categoryOptions.add(option);
        }

        List<Unit> allUnits = unitRepository.findAll();
        List<Map<String, Object>> unitOptions = new ArrayList<>();
        for (Unit unit : allUnits) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", unit.getUnitId());
            option.put("name", unit.getUnitName());
            if (product.getUnitId() != null && product.getUnitId().equals(unit.getUnitId())) {
                option.put("selected", true);
            }
            unitOptions.add(option);
        }

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
        model.addAttribute("categories", categoryOptions);
        model.addAttribute("units", unitOptions);

        return "product/product_form";
    }

    /**
     * Processes the submission of the product form.
     * <p><b>Use Case:</b> This endpoint is called via a POST request when the user clicks "Save"
     * on the product form.
     *
     * @param productDto The DTO populated with form data, by Spring's @ModelAttribute data binding.
     * @param redirectAttributes Used to pass messages (success or error) to the next request after a redirect.
     * @return A redirect string to either the product form (on success) or back to the form (on error).
     */
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

    /**
     * Displays the list of all products, with optional filtering.
     * <p><b>Use Case:</b> This is the main landing page for the product management feature. It's also
     * called when a user performs a search or filters the list.
     *
     * @param keyword The search term entered by the user. Optional.
     * @param category The category name selected by the user for filtering. Optional.
     * @param model The Spring Model to pass data to the view.
     * @return The path to the product list view template.
     */
    @GetMapping
    public String showProductList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Model model) {

        List<ProductDto> products;
        if ((keyword != null && !keyword.isEmpty()) || (category != null && !category.isEmpty())) {
            products = productService.search(keyword, category);
        } else {
            products = productService.findAll();
        }

        model.addAttribute("products", products);
        model.addAttribute("isProductPage", true);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", categoryRepository.findAll());

        return "product/product_list";
    }

    /**
     * Deletes one or more products based on their IDs.
     * <p><b>Use Case:</b> Called via a POST request when the user selects products from the list
     * and clicks the "Delete" button.
     *
     * @param ids A list of product IDs to be deleted, captured from the form submission.
     * @param redirectAttributes Used to show a success or error message after the operation.
     * @return A redirect string back to the product list page.
     */
    @PostMapping("/delete")
    public String deleteProducts(
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
