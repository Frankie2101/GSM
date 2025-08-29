package com.gsm.controller;

import com.gsm.dto.BOMTemplateDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.model.ProductCategory;
import com.gsm.repository.ProductCategoryRepository;
import com.gsm.service.BOMTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/bom-templates")
public class BOMTemplateController {

    @Autowired private BOMTemplateService bomTemplateService;
    @Autowired private ProductCategoryRepository categoryRepository;

    @GetMapping("/form")
    public String showBomTemplateForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) {
        BOMTemplateDto template;
        if (id != null) {
            template = bomTemplateService.findById(id);
        } else {
            template = new BOMTemplateDto();
            template.setDetails(new ArrayList<>());
        }

        // Logic xử lý Product Category Dropdown
        List<ProductCategory> allCategories = categoryRepository.findAll();
        List<Map<String, Object>> categoryOptions = new ArrayList<>();
        for (ProductCategory category : allCategories) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", category.getCategoryId());
            option.put("name", category.getCategoryName());
            if (template.getProductCategoryId() != null && template.getProductCategoryId().equals(category.getCategoryId())) {
                option.put("selected", true);
            }
            categoryOptions.add(option);
        }

        model.addAttribute("template", template);
        model.addAttribute("isBomTemplatePage", true);
        model.addAttribute("categories", categoryOptions);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));

        return "bom-template/bom_template_form";
    }

    @PostMapping("/save")
    public String saveBomTemplate(@ModelAttribute BOMTemplateDto bomTemplateDto, RedirectAttributes redirectAttributes) {
        try {
            BOMTemplateDto savedTemplate = bomTemplateService.save(bomTemplateDto);
            redirectAttributes.addFlashAttribute("successMessage", "Saved Successfully!");
            return "redirect:/bom-templates/form?id=" + savedTemplate.getBomTemplateId();
        } catch (DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("template", bomTemplateDto);
            if (bomTemplateDto.getBomTemplateId() == null) {
                return "redirect:/bom-templates/form";
            }
            return "redirect:/bom-templates/form?id=" + bomTemplateDto.getBomTemplateId();
        }
    }

    @GetMapping
    public String showBomTemplateList(@RequestParam(required = false) String keyword, Model model, HttpServletRequest request) {
        List<BOMTemplateDto> templates;
        if (keyword != null && !keyword.isEmpty()) {
            templates = bomTemplateService.search(keyword);
        } else {
            templates = bomTemplateService.findAll();
        }
        model.addAttribute("templates", templates);
        model.addAttribute("isBomTemplatePage", true);
        model.addAttribute("keyword", keyword);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "bom-template/bom_template_list";
    }

    @PostMapping("/delete")
    public String deleteBomTemplates(@RequestParam(value = "selectedIds", required = false) List<Long> ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one template to delete.");
        } else {
            bomTemplateService.deleteByIds(ids);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted " + ids.size() + " template(s).");
        }
        return "redirect:/bom-templates";
    }
}
