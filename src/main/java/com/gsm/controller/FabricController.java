package com.gsm.controller;

import com.gsm.dto.FabricDto;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.model.Supplier;
import com.gsm.model.Unit;
import com.gsm.repository.SupplierRepository;
import com.gsm.repository.UnitRepository;
import com.gsm.service.FabricService;
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
@RequestMapping("/fabrics")
public class FabricController {

    @Autowired private FabricService fabricService;
    // THÊM MỚI: Inject các repository cần thiết cho dropdowns
    @Autowired private UnitRepository unitRepository;
    @Autowired private SupplierRepository supplierRepository;

    @GetMapping
    public String showFabricList(@RequestParam(required = false) String keyword, Model model, HttpServletRequest request) {
        List<FabricDto> fabrics;
        if (keyword != null && !keyword.isEmpty()) {
            fabrics = fabricService.search(keyword);
        } else {
            fabrics = fabricService.findAll();
        }
        model.addAttribute("fabrics", fabrics);
        model.addAttribute("isFabricPage", true);
        model.addAttribute("keyword", keyword);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "fabric/fabric_list";
    }

    /**
     * THÊM MỚI: Hiển thị form tạo mới hoặc chỉnh sửa Fabric.
     */
    @GetMapping("/form")
    public String showFabricForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) {
        FabricDto fabric;
        if (id != null) {
            fabric = fabricService.findById(id);
        } else {
            fabric = new FabricDto();
            fabric.setFabricColors(new ArrayList<>()); // Khởi tạo list rỗng
        }

        // Logic xử lý Unit Dropdown
        List<Unit> allUnits = unitRepository.findAll();
        List<Map<String, Object>> unitOptions = new ArrayList<>();
        for (Unit unit : allUnits) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", unit.getUnitId());
            option.put("name", unit.getUnitName());
            if (fabric.getUnitId() != null && fabric.getUnitId().equals(unit.getUnitId())) {
                option.put("selected", true);
            }
            unitOptions.add(option);
        }

        // Logic xử lý Supplier Dropdown
        List<Supplier> allSuppliers = supplierRepository.findAll();
        List<Map<String, Object>> supplierOptions = new ArrayList<>();
        for (Supplier supplier : allSuppliers) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", supplier.getSupplierId());
            option.put("name", supplier.getSupplierName());
            if (fabric.getSupplierId() != null && fabric.getSupplierId().equals(supplier.getSupplierId())) {
                option.put("selected", true);
            }
            supplierOptions.add(option);
        }

        model.addAttribute("fabric", fabric);
        model.addAttribute("isFabricPage", true);
        model.addAttribute("units", unitOptions);
        model.addAttribute("suppliers", supplierOptions);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));

        return "fabric/fabric_form";
    }

    /**
     * THÊM MỚI: Xử lý lưu thông tin Fabric.
     */
    @PostMapping("/save")
    public String saveFabric(@ModelAttribute FabricDto fabricDto, RedirectAttributes redirectAttributes) {
        try {
            FabricDto savedFabric = fabricService.save(fabricDto);
            redirectAttributes.addFlashAttribute("successMessage", "Saved Successfully!");
            return "redirect:/fabrics/form?id=" + savedFabric.getFabricId();
        } catch (DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("fabric", fabricDto); // Giữ lại dữ liệu đã nhập
            // Nếu là tạo mới và bị lỗi, quay lại form rỗng
            if (fabricDto.getFabricId() == null) {
                return "redirect:/fabrics/form";
            }
            // Nếu là cập nhật và bị lỗi, quay lại form edit
            return "redirect:/fabrics/form?id=" + fabricDto.getFabricId();
        }
    }

    @PostMapping("/delete")
    public String deleteFabrics(@RequestParam(value = "selectedIds", required = false) List<Long> ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one fabric to delete.");
        } else {
            fabricService.deleteByIds(ids);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted " + ids.size() + " fabric(s).");
        }
        return "redirect:/fabrics";
    }
}
