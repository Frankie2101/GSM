package com.gsm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsm.dto.SaleOrderDto;
import com.gsm.enums.SaleOrderStatus; // Import SaleOrderStatus
import com.gsm.exception.DuplicateResourceException;
import com.gsm.model.Customer;
import com.gsm.repository.CustomerRepository;
import com.gsm.service.SaleOrderService;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
@RequestMapping("/sale-orders")
public class SaleOrderController {

    @Autowired private SaleOrderService saleOrderService;
    @Autowired private CustomerRepository customerRepository;

    @GetMapping
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

    @PostMapping("/delete")
    public String deleteSaleOrders(@RequestParam(value = "selectedIds", required = false) List<Long> ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one order to delete.");
        } else {
            saleOrderService.deleteByIds(ids);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted " + ids.size() + " order(s).");
        }
        return "redirect:/sale-orders";
    }

    @GetMapping("/form")
    public String showSaleOrderForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) throws JsonProcessingException {
        SaleOrderDto order;
        if (id != null) {
            order = saleOrderService.findById(id);
        } else {
            order = new SaleOrderDto();
            order.setStatus(SaleOrderStatus.New);
            // YÊU CẦU 2: Tự động điền ngày hôm nay
            order.setOrderDate(LocalDate.now());
            order.setDetails(new ArrayList<>());
        }

        // Logic xử lý Customer Dropdown
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

        // ... (phần còn lại của phương thức giữ nguyên)
        List<Map<String, Object>> statusOptions = new ArrayList<>();
        for (SaleOrderStatus status : SaleOrderStatus.values()) {
            Map<String, Object> option = new HashMap<>();
            option.put("name", status.name());
            if (order.getStatus() == status) {
                option.put("selected", true);
            }
            statusOptions.add(option);
        }

        if (order.getDetails() != null && !order.getDetails().isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            String detailsJson = mapper.writeValueAsString(order.getDetails());
            model.addAttribute("detailsJson", detailsJson);
        } else {
            model.addAttribute("detailsJson", "[]"); // Trả về mảng rỗng nếu không có details
        }

        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        model.addAttribute("order", order);
        model.addAttribute("isSaleOrderPage", true);
        model.addAttribute("customers", customerOptions);
        model.addAttribute("statuses", statusOptions);

        return "sale-order/sale_order_form";
    }

    @PostMapping("/save")
    public String saveSaleOrder(@ModelAttribute SaleOrderDto saleOrderDto, RedirectAttributes redirectAttributes) {
        try {
            SaleOrderDto savedOrder = saleOrderService.save(saleOrderDto);
            redirectAttributes.addFlashAttribute("successMessage", "Saved Successfully!");
            return "redirect:/sale-orders/form?id=" + savedOrder.getSaleOrderId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("order", saleOrderDto);
            if (saleOrderDto.getSaleOrderId() == null) {
                return "redirect:/sale-orders/form";
            }
            return "redirect:/sale-orders/form?id=" + saleOrderDto.getSaleOrderId();
        }
    }

}