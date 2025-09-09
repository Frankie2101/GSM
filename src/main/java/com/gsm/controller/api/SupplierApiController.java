package com.gsm.controller.api;

import com.gsm.dto.SupplierDto;
import com.gsm.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierApiController {

    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping
    public ResponseEntity<List<SupplierDto>> getAllSuppliers() {
        List<SupplierDto> suppliers = supplierRepository.findAll().stream()
                .map(supplier -> new SupplierDto(
                        supplier.getSupplierId(),
                        supplier.getSupplierName(),
                        supplier.getCurrencyCode()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(suppliers);
    }
}