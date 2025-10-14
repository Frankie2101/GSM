package com.gsm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gsm.dto.PurchaseOrderDto;
import com.gsm.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import com.samskivert.mustache.Mustache;
import com.gsm.repository.MaterialGroupRepository;


import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;

/**
 * Controller for handling user-facing HTTP requests for the Purchase Order (PO) feature.
 * This controller is primarily responsible for serving the HTML pages (views) that
 * act as a shell for the JavaScript-driven user interface.
 */
@Controller
@RequestMapping("/purchase_orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;
    @Autowired
    private MaterialGroupRepository materialGroupRepository;

    /**
     * Displays the main list page for all Purchase Orders.
     * The actual data loading is handled by JavaScript via API calls.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('PURCHASE_ORDER_VIEW')")
    public String showPoListPage(Model model, HttpServletRequest request) {
        model.addAttribute("isPurchaseOrderPage", true);
        model.addAttribute("materialGroups", materialGroupRepository.findAll());

        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "po/po_list";
    }

    /**
     * Displays the form for creating or viewing/editing a Purchase Order.
     * This method passes the entire PO data as a single JSON string ("poJson") to the view.
     * The JavaScript on the frontend will then parse this JSON to hydrate and render the form.
     * @param id The ID of the PO to edit (null for creation).
     * @return The path to the PO form view.
     */
    @GetMapping("/form")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('PURCHASE_ORDER_VIEW')")
    public String showPoForm(@RequestParam(required = false) Long id, Model model, HttpServletRequest request) throws JsonProcessingException {
        PurchaseOrderDto poDto = (id != null) ? purchaseOrderService.findById(id) : new PurchaseOrderDto();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        model.addAttribute("poJson", mapper.writeValueAsString(poDto));
        model.addAttribute("isPurchaseOrderPage", true);
        model.addAttribute("materialGroups", materialGroupRepository.findAll());
        return "po/po_form";
    }

    /**
     * Displays the page for approving POs ("Pending Approval").
     */
    @GetMapping("/pending-approval")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('PURCHASE_ORDER_APPROVE')")
    public String showPendingApprovalPage(Model model) {
        model.addAttribute("isPurchaseOrderPage", true);
        return "po/pending_approval_list";
    }

    /**
     * A Mustache.Lambda function that provides server-side number formatting.
     * This allows the template to format numbers without containing complex logic.
     * Usage in Mustache: {{#FormatNumber}}{{totalAmount}}{{/FormatNumber}}
     * @return A Mustache.Lambda that formats numbers as #,##0.00.
     */
    private Mustache.Lambda formatNumberLambda() {
        return (frag, out) -> {
            try {
                String text = frag.execute();
                if (text == null || text.isEmpty()) {
                    return;
                }
                double number = Double.parseDouble(text);
                DecimalFormat formatter = new DecimalFormat("#,##0.00");
                out.write(formatter.format(number));
            } catch (NumberFormatException e) {
                out.write(frag.execute());
            }
        };
    }

    /**
     * Displays a printable view of an approved Purchase Order.
     * @param id The ID of the PO to print.
     * @return The path to the printable PO view.
     */
    @GetMapping("/print/{id}")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('PURCHASE_ORDER_VIEW') or hasAuthority('PURCHASE_ORDER_APPROVE')")
    public String showPrintPoPage(@PathVariable Long id, Model model, HttpServletRequest request) {
        PurchaseOrderDto poDto = purchaseOrderService.findById(id);

        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        model.addAttribute("po", poDto);
        model.addAttribute("FormatNumber", formatNumberLambda());

        model.addAttribute("isPurchaseOrderPage", true);
        return "po/po_approval_view";
    }

    /**
     * Displays a print-friendly preview of a PO.
     * @param id The ID of the PO to preview.
     * @return The path to the print preview template.
     */
    @GetMapping("/print-preview/{id}")
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('PURCHASE_ORDER_PRINT')")
    public String showPrintPreviewPage(@PathVariable Long id, Model model) {
        PurchaseOrderDto poDto = purchaseOrderService.findById(id);
        model.addAttribute("po", poDto);
        model.addAttribute("FormatNumber", formatNumberLambda());
        return "po/po_print_preview";
    }

}