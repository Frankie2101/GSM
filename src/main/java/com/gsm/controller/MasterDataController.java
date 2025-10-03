package com.gsm.controller;

import com.gsm.repository.CustomerRepository;
import com.gsm.repository.SupplierRepository;
import com.gsm.repository.UnitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/master-data")
public class MasterDataController {

    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final UnitRepository unitRepository;

    public MasterDataController(CustomerRepository customerRepository, SupplierRepository supplierRepository, UnitRepository unitRepository) {
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.unitRepository = unitRepository;
    }

    @GetMapping
    public String showMasterDataPage(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
        model.addAttribute("units", unitRepository.findAll());

        model.addAttribute("isMasterDataPage", true);

        return "master/master_data";
    }

    /**
     * Handles requests to download a master data template file.
     *
     * @param fileName The name of the file to be downloaded (e.g., "Customer_Import_Template.xlsx").
     * @return A ResponseEntity containing the file resource for the browser to download.
     */
    @GetMapping("/download-template/{fileName:.+}")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable String fileName) {
        try {
            Resource resource = new ClassPathResource("static/templates/" + fileName);

            if (resource.exists()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}