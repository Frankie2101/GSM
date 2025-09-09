package com.gsm.controller;

import com.gsm.dto.ProductionOutputDto;
import com.gsm.service.ProductionOutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/production-outputs")
public class ProductionOutputController {

    @Autowired
    private ProductionOutputService productionOutputService;

    // YÊU CẦU MỚI: Nhận thêm các tham số tìm kiếm
    @GetMapping
    public String showProductionOutputList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate outputDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate outputDateTo,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String productionLine,
            Model model, HttpServletRequest request) {

        // Gọi service với đầy đủ tham số
        List<ProductionOutputDto> outputs = productionOutputService.search(keyword, outputDateFrom, outputDateTo, department, productionLine);

        model.addAttribute("outputs", outputs);
        model.addAttribute("isProductionOutputPage", true);

        // Đưa các giá trị tìm kiếm trở lại view để giữ lại trên form
        model.addAttribute("keyword", keyword);
        model.addAttribute("outputDateFrom", outputDateFrom);
        model.addAttribute("outputDateTo", outputDateTo);
        model.addAttribute("department", department);
        model.addAttribute("productionLine", productionLine);

        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "production-output/production_output_list";
    }

    @PostMapping("/delete")
    public String deleteProductionOutputs(@RequestParam(value = "selectedIds", required = false) List<Long> ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one output to delete.");
        } else {
            productionOutputService.deleteByIds(ids);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted " + ids.size() + " output(s).");
        }
        return "redirect:/production-outputs";
    }
}
