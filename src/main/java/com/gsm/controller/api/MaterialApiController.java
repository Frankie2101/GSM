package com.gsm.controller.api;

import com.gsm.model.Fabric;
import com.gsm.model.Supplier;
import com.gsm.model.Trim;
import com.gsm.repository.FabricRepository;
import com.gsm.repository.TrimRepository;
import com.gsm.model.TrimVariant; // <-- SỬA LỖI 1: THÊM IMPORT
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MaterialApiController {

    @Autowired private FabricRepository fabricRepository;
    @Autowired private TrimRepository trimRepository;

    /**
     * API 1: Lấy danh sách Code và ID để hiển thị trong dropdown.
     */
    @GetMapping("/materials")
    public ResponseEntity<List<MaterialSelectionInfo>> getMaterialList(@RequestParam String type) {
        if ("FA".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(fabricRepository.findAll().stream()
                    .map(f -> new MaterialSelectionInfo(f.getFabricId(), f.getFabricCode()))
                    .collect(Collectors.toList()));
        } else if ("TR".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(trimRepository.findAll().stream()
                    .map(t -> new MaterialSelectionInfo(t.getTrimId(), t.getTrimCode()))
                    .collect(Collectors.toList()));
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    /**
     * API 2: Lấy thông tin chi tiết của một material sau khi người dùng đã chọn.
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

    // === API MỚI 1: LẤY DANH SÁCH MÀU SẮC ===
    @GetMapping("/material-colors")
    public ResponseEntity<List<ColorInfo>> getMaterialColors(@RequestParam String type, @RequestParam Long materialId) {
        if ("FA".equalsIgnoreCase(type)) {
            return fabricRepository.findById(materialId)
                    .map(fabric -> {
                        List<ColorInfo> colors = fabric.getFabricColors().stream()
                                .map(fc -> new ColorInfo(fc.getColor(), fc.getColorName(), fc.getNetPrice()))
                                .collect(Collectors.toList());
                        return ResponseEntity.ok(colors);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } else if ("TR".equalsIgnoreCase(type)) {
            return trimRepository.findById(materialId)
                    .map(trim -> {
                        // Lấy các màu sắc duy nhất từ variant
                        List<ColorInfo> colors = trim.getVariants().stream()
                                .collect(Collectors.toMap(TrimVariant::getColorCode, tv -> new ColorInfo(tv.getColorCode(), tv.getColorName(), tv.getNetPrice()), (existing, replacement) -> existing))
                                .values().stream().collect(Collectors.toList());
                        return ResponseEntity.ok(colors);
                    })
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().build();
    }

    // === API MỚI 2: LẤY DANH SÁCH SIZE DỰA TRÊN MÀU SẮC (CHỈ DÀNH CHO TRIM) ===
    @GetMapping("/material-sizes")
    public ResponseEntity<List<SizeInfo>> getMaterialSizes(@RequestParam Long trimId, @RequestParam String colorCode) {
        return trimRepository.findById(trimId)
                .map(trim -> {
                    List<SizeInfo> sizes = trim.getVariants().stream()
                            .filter(v -> colorCode.equals(v.getColorCode()))
                            .map(v -> new SizeInfo(v.getSizeCode(), v.getNetPrice()))
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(sizes);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- CÁC HÀM HELPER ĐỂ CHUYỂN ĐỔI ---

    private MaterialDetailInfo convertFabricToDetailInfo(Fabric f) {
        Supplier s = f.getSupplier();
        // Lấy giá từ FabricColor đầu tiên tìm thấy (hoặc null nếu không có)
        Double price = f.getFabricColors().stream()
                .findFirst()
                .map(fc -> fc.getNetPrice())
                .orElse(null);

        return new MaterialDetailInfo(
                f.getFabricName(),
                f.getUnit() != null ? f.getUnit().getUnitName() : "",
                s != null ? s.getSupplierName() : "",
                price,
                s != null ? s.getCurrencyCode() : ""
        );
    }

    private MaterialDetailInfo convertTrimToDetailInfo(Trim t) {
        Supplier s = t.getSupplier();
        // Lấy giá từ TrimVariant đầu tiên tìm thấy (hoặc null nếu không có)
        Double price = t.getVariants().stream()
                .findFirst()
                .map(tv -> tv.getNetPrice())
                .orElse(null);

        return new MaterialDetailInfo(
                t.getTrimName(),
                t.getUnit() != null ? t.getUnit().getUnitName() : "",
                s != null ? s.getSupplierName() : "",
                price,
                s != null ? s.getCurrencyCode() : ""
        );
    }

    // --- INNER CLASSES CHO CÁC API RESPONSE ---

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
        private String supplier;
        private Double price;
        private String currency;
    }

    @Data
    @AllArgsConstructor
    public static class ColorInfo {
        private String code;
        private String name;
        private Double price;
    }


    @Data
    @AllArgsConstructor
    public static class SizeInfo {
        private String size;
        private Double price;
    }
}