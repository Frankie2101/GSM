package com.gsm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsm.dto.SaleOrderDto;
import com.gsm.enums.SaleOrderStatus;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.model.Customer;
import com.gsm.repository.CustomerRepository;
import com.gsm.service.SaleOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


/**
 * Controller for handling user-facing HTTP requests for the Sale Order management feature.
 * This includes displaying the sale order list and form, and processing save/delete actions.
 */
@Controller
@RequestMapping("/sale-orders")
public class SaleOrderController {

    @Autowired private SaleOrderService saleOrderService;
    @Autowired private CustomerRepository customerRepository;

    /**
     * Displays the list of all sale orders, with an optional keyword search.
     * <p><b>Use Case:</b> The main landing page for sale order management.
     * @param keyword Optional search term.
     * @param model   The Spring Model for passing data to the view.
     * @param request The HttpServletRequest for retrieving the CSRF token.
     * @return The path to the sale order list view.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('SALE_ORDER_VIEW')")
    public String showSaleOrderList(@RequestParam(required = false) String keyword, Model model, HttpServletRequest request) {
        List<SaleOrderDto> orders;
        if (keyword != null && !keyword.isEmpty()) {
            orders = saleOrderService.search(keyword);
        } else {
            orders = saleOrderService.findAll();
        }
        model.addAttribute("orders", orders);
        model.addAttribute("isSaleOrderPage", true);
        model.addAttribute("keyword", keyword);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "sale-order/sale_order_list";
    }

    /**
     * Displays the form for creating a new sale order or editing an existing one.
     * <p><b>Use Case:</b> Called when a user clicks "Create" or "Edit".
     * @param id      The ID of the sale order to edit (null for creation).
     * @param model   The Spring Model.
     * @param request The HttpServletRequest.
     * @return The path to the sale order form view.
     * @throws JsonProcessingException if there's an error converting the details list to JSON.
     */
    @GetMapping("/form")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('SALE_ORDER_VIEW')")
    public String showSaleOrderForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) throws JsonProcessingException {
        SaleOrderDto order;
        if (id != null) {
            order = saleOrderService.findById(id);
        } else {
            order = new SaleOrderDto();
            order.setStatus(SaleOrderStatus.New);
            order.setOrderDate(LocalDate.now());
            order.setDetails(new ArrayList<>());
        }

        // Prepare dropdown data and pre-select options based on the current order data.
        List<Customer> allCustomers = customerRepository.findAll();
        List<Map<String, Object>> customerOptions = new ArrayList<>();
        for (Customer customer : allCustomers) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", customer.getCustomerId());
            option.put("name", customer.getCustomerName());
            option.put("code", customer.getCustomerCode());
            option.put("currency", customer.getCurrencyCode());
            if (order.getCustomerId() != null && order.getCustomerId().equals(customer.getCustomerId())) {
                option.put("selected", true);
            }
            customerOptions.add(option);
        }

        List<Map<String, Object>> statusOptions = new ArrayList<>();
        for (SaleOrderStatus status : SaleOrderStatus.values()) {
            Map<String, Object> option = new HashMap<>();
            option.put("name", status.name());
            if (order.getStatus() == status) {
                option.put("selected", true);
            }
            statusOptions.add(option);
        }

        // Convert the details list to a JSON string to be used by the frontend JavaScript.
        // This allows the frontend to dynamically render the details table.
        if (order.getDetails() != null && !order.getDetails().isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            String detailsJson = mapper.writeValueAsString(order.getDetails());
            model.addAttribute("detailsJson", detailsJson);
        } else {
            model.addAttribute("detailsJson", "[]");
        }

        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        model.addAttribute("order", order);
        model.addAttribute("isSaleOrderPage", true);
        model.addAttribute("customers", customerOptions);
        model.addAttribute("statuses", statusOptions);

        return "sale-order/sale_order_form";
    }

    /**
     * Processes the submission of the sale order form.
     * <p><b>Use Case:</b> Called via POST when the user clicks "Save" on the form.
     * @param saleOrderDto       The DTO populated with form data.
     * @param redirectAttributes For passing flash messages after a redirect.
     * @return A redirect string to the appropriate page.
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('SALE_ORDER_CREATE_EDIT')")
    public String saveSaleOrder(@ModelAttribute SaleOrderDto saleOrderDto, RedirectAttributes redirectAttributes) {
        try {
            SaleOrderDto savedOrder = saleOrderService.save(saleOrderDto);
            redirectAttributes.addFlashAttribute("successMessage", "Saved Successfully!");
            return "redirect:/sale-orders/form?id=" + savedOrder.getSaleOrderId();
        } catch (Exception e) {
            // SUGGESTION: Catch specific exceptions
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("order", saleOrderDto);
            if (saleOrderDto.getSaleOrderId() == null) {
                return "redirect:/sale-orders/form";
            }
            return "redirect:/sale-orders/form?id=" + saleOrderDto.getSaleOrderId();
        }
    }

    /**
     * Deletes one or more sale orders based on a list of selected IDs.
     * <p><b>Use Case:</b> Called when the user clicks "Delete" on the list page.
     * @param ids                The list of sale order IDs to delete.
     * @param redirectAttributes For passing flash messages.
     * @return A redirect string back to the sale order list.
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('SALE_ORDER_DELETE')")
    public String deleteSaleOrders(@RequestParam(value = "selectedIds", required = false) List<Long> ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one order to delete.");
        } else {
            saleOrderService.deleteByIds(ids);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted " + ids.size() + " order(s).");
        }
        return "redirect:/sale-orders";
    }

}