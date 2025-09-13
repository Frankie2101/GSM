// trong file: com/gsm/controller/PurchaseOrderController.java

package com.gsm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gsm.dto.PurchaseOrderDto;
import com.gsm.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
// Thêm 2 import này vào đầu file
import com.samskivert.mustache.Mustache;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;

@Controller
@RequestMapping("/purchase_orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    // Hiển thị trang danh sách tất cả PO
    @GetMapping
    public String showPoListPage(Model model) {
        model.addAttribute("isPurchaseOrderPage", true);
        return "po/po_list";
    }

    // Hiển thị form tạo mới hoặc xem/sửa PO
    @GetMapping("/form")
    public String showPoForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) throws JsonProcessingException {
        PurchaseOrderDto poDto = (id != null) ? purchaseOrderService.findById(id) : new PurchaseOrderDto();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        model.addAttribute("poJson", mapper.writeValueAsString(poDto));
        model.addAttribute("isPurchaseOrderPage", true);
        return "po/po_form";
    }

    // Hiển thị trang duyệt PO "Pending Approval"
    @GetMapping("/pending-approval")
    public String showPendingApprovalPage(Model model) {
        model.addAttribute("isPurchaseOrderPage", true);
        return "po/pending_approval_list";
    }

    // Thêm hàm private này vào trong class PurchaseOrderController
    private Mustache.Lambda formatNumberLambda() {
        return (frag, out) -> {
            try {
                // Lấy giá trị số từ template
                String text = frag.execute();
                if (text == null || text.isEmpty()) {
                    return;
                }
                double number = Double.parseDouble(text);
                // Định dạng lại theo chuẩn #,##0.00
                DecimalFormat formatter = new DecimalFormat("#,##0.00");
                out.write(formatter.format(number));
            } catch (NumberFormatException e) {
                // Nếu không phải là số, ghi lại giá trị gốc
                out.write(frag.execute());
            }
        };
    }

    //Pending Approval Print
    @GetMapping("/print/{id}")
    public String showPrintPoPage(@PathVariable Long id, Model model, HttpServletRequest request) {
        PurchaseOrderDto poDto = purchaseOrderService.findById(id);

        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        model.addAttribute("po", poDto);
        // THÊM DÒNG NÀY
        model.addAttribute("FormatNumber", formatNumberLambda());

        model.addAttribute("isPurchaseOrderPage", true);
        return "po/po_approval_view";
    }

    // Thêm phương thức này vào PurchaseOrderController
    @GetMapping("/print-preview/{id}")
    public String showPrintPreviewPage(@PathVariable Long id, Model model) {
        PurchaseOrderDto poDto = purchaseOrderService.findById(id);
        model.addAttribute("po", poDto);
        model.addAttribute("FormatNumber", formatNumberLambda());
        return "po/po_print_preview"; // Trỏ đến file preview mới
    }
}