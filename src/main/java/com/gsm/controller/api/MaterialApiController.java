package com.gsm.controller.api;

import com.gsm.model.Fabric;
import com.gsm.model.Supplier;
import com.gsm.model.Trim;
import com.gsm.repository.FabricRepository;
import com.gsm.repository.TrimRepository;
import com.gsm.model.TrimVariant;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller that provides API endpoints related to raw materials (Fabrics and Trims).
 * These APIs are consumed by the dynamic Order BOM form to populate dropdowns and retrieve details.
 */
@RestController
@RequestMapping("/api")
public class MaterialApiController {

    @Autowired private FabricRepository fabricRepository;
    @Autowired private TrimRepository trimRepository;

    /**
     * API to get a list of materials (ID and Code only) for selection dropdowns.
     * @param type The type of material to fetch ("FA" for Fabric, "TR" for Trim).
     * @return A list of simplified material info.
     */
    @GetMapping("/materials")
    public ResponseEntity<List<MaterialSelectionInfo>> getMaterialList(@RequestParam String type, @RequestParam(required = false) Long materialGroupId) {
        if (materialGroupId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        if ("FA".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(fabricRepository.findByMaterialGroup_MaterialGroupId(materialGroupId).stream()
                    .map(f -> new MaterialSelectionInfo(f.getFabricId(), f.getFabricCode()))
                    .collect(Collectors.toList()));
        } else if ("TR".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(trimRepository.findByMaterialGroup_MaterialGroupId(materialGroupId).stream()
                    .map(t -> new MaterialSelectionInfo(t.getTrimId(), t.getTrimCode()))
                    .collect(Collectors.toList()));
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    /**
     * API to get detailed information of a single material after a user selects it.
     * @param id The ID of the Fabric or Trim.
     * @param type The type of material ("FA" or "TR").
     * @return Detailed info used to auto-fill form fields.
     */
    @GetMapping("/material-details/{id}")
    public ResponseEntity<MaterialDetailInfo> getMaterialDetails(
            @PathVariable Long id,
            @RequestParam String type) {

        if ("FA".equalsIgnoreCase(type)) {
            return fabricRepository.findById(id).map(this::convertFabricToDetailInfo)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else if ("TR".equalsIgnoreCase(type)) {
            return trimRepository.findById(id).map(this::convertTrimToDetailInfo)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * API to get the list of available colors for a given material.
     * For Trims, it cleverly extracts a unique list of colors from its variants.
     */
    @GetMapping("/material-colors")
    public ResponseEntity<List<ColorInfo>> getMaterialColors(@RequestParam String type, @RequestParam Long materialId) {
        if ("FA".equalsIgnoreCase(type)) {
            // For Fabric, colors are a direct child collection.
            return fabricRepository.findById(materialId)
                    .map(fabric -> {
                        List<ColorInfo> colors = fabric.getFabricColors().stream()
                                .map(fc -> new ColorInfo(fc.getColor(), fc.getColorName(), fc.getNetPrice(), 0.0))
                                .collect(Collectors.toList());
                        return ResponseEntity.ok(colors);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } else if ("TR".equalsIgnoreCase(type)) {
            return trimRepository.findById(materialId)
                    .map(trim -> {
                        // For Trim, multiple variants can have the same color. We need a unique list.
                        // Collectors.toMap is used here to get distinct colors.
                        List<ColorInfo> colors = trim.getVariants().stream()
                                .collect(Collectors.toMap(
                                        TrimVariant::getColorCode, // Key for the map is the color code.
                                        tv -> new ColorInfo(tv.getColorCode(), tv.getColorName(), tv.getNetPrice(), tv.getTaxRate()), // Value is the ColorInfo object.
                                        (existing, replacement) -> existing // Merge function: if a key collision occurs (duplicate color), keep the existing one.
                                ))
                                .values().stream().collect(Collectors.toList());
                        return ResponseEntity.ok(colors);
                    })
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * API to get available sizes for a specific Trim and color combination.
     */
    @GetMapping("/material-sizes")
    public ResponseEntity<List<SizeInfo>> getMaterialSizes(@RequestParam Long trimId, @RequestParam String colorCode) {
        return trimRepository.findById(trimId)
                .map(trim -> {
                    List<SizeInfo> sizes = trim.getVariants().stream()
                            .filter(v -> colorCode.equals(v.getColorCode()))
                            .map(v -> new SizeInfo(v.getSizeCode(), v.getNetPrice(), v.getTaxRate())).collect(Collectors.toList());
                    return ResponseEntity.ok(sizes);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Helper methods for conversion to DTOs ---
    private MaterialDetailInfo convertFabricToDetailInfo(Fabric f) {
        Supplier s = f.getSupplier();
        Double price = f.getFabricColors().stream()
                .findFirst()
                .map(fc -> fc.getNetPrice())
                .orElse(null);

        return new MaterialDetailInfo(
                f.getFabricName(),
                f.getUnit() != null ? f.getUnit().getUnitName() : "",
                s != null ? s.getSupplierId() : null,
                s != null ? s.getSupplierName() : "",
                price,
                s != null ? s.getCurrencyCode() : ""
        );
    }

    private MaterialDetailInfo convertTrimToDetailInfo(Trim t) {
        Supplier s = t.getSupplier();
        Double price = t.getVariants().stream()
                .findFirst()
                .map(tv -> tv.getNetPrice())
                .orElse(null);

        return new MaterialDetailInfo(
                t.getTrimName(),
                t.getUnit() != null ? t.getUnit().getUnitName() : "",
                s != null ? s.getSupplierId() : null,
                s != null ? s.getSupplierName() : "",
                price,
                s != null ? s.getCurrencyCode() : ""
        );
    }

    // --- Inner classes for structuring API JSON responses ---
    @Data
    @AllArgsConstructor
    public static class MaterialSelectionInfo {
        private Long id;
        private String code;
    }

    @Data
    @AllArgsConstructor
    public static class MaterialDetailInfo {
        private String name;
        private String unitName;
        private Long supplierId;
        private String supplierName;
        private Double price;
        private String currency;
    }

    @Data
    @AllArgsConstructor
    public static class ColorInfo {
        private String code;
        private String name;
        private Double price;
        private Double taxRate;
    }


    @Data
    @AllArgsConstructor
    public static class SizeInfo {
        private String size;
        private Double price;
        private Double taxRate;
    }
}