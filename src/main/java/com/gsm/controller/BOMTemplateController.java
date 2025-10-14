package com.gsm.controller;

import com.gsm.dto.BOMTemplateDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.model.ProductCategory;
import com.gsm.repository.ProductCategoryRepository;
import com.gsm.service.BOMTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.gsm.model.MaterialGroup;
import com.gsm.repository.MaterialGroupRepository;

/**
 * Controller for handling all user-facing HTTP requests for the BOM Template feature.
 */
@Controller
@RequestMapping("/bom-templates")
public class BOMTemplateController {

    @Autowired private BOMTemplateService bomTemplateService;
    @Autowired private ProductCategoryRepository categoryRepository;
    @Autowired private MaterialGroupRepository materialGroupRepository;

    /**
     * Displays the form for creating a new BOM Template or editing an existing one.
     * <p><b>Use Case:</b> Called when a user clicks "Create" or "Edit".
     */
    @GetMapping("/form")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('BOM_TEMPLATE_VIEW')")
    public String showBomTemplateForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) {
        BOMTemplateDto template;
        if (id != null) {
            template = bomTemplateService.findById(id);
        } else {
            template = new BOMTemplateDto();
            template.setDetails(new ArrayList<>());
        }

        // Logic to prepare the Product Category dropdown and pre-select the correct option.
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

        List<MaterialGroup> allMaterialGroups = materialGroupRepository.findAll();

        model.addAttribute("materialGroups", allMaterialGroups);
        model.addAttribute("template", template);
        model.addAttribute("isBomTemplatePage", true);
        model.addAttribute("categories", categoryOptions);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));

        return "bom-template/bom_template_form";
    }

    /**
     * Processes the submission of the BOM Template form.
     * <p><b>Use Case:</b> Called via POST when the user clicks "Save" on the form.
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('BOM_TEMPLATE_CREATE_EDIT')")
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

    /**
     * Displays the list of all BOM Templates, with optional filtering.
     * <p><b>Use Case:</b> The main landing page for BOM Template management.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('BOM_TEMPLATE_VIEW')")
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

    /**
     * Deletes one or more BOM Templates based on a list of selected IDs.
     * <p><b>Use Case:</b> Called when the user clicks "Delete" on the list page.
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('BOM_TEMPLATE_DELETE')")
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
