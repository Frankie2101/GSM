package com.gsm.controller.api;

import com.gsm.dto.*;
import com.gsm.service.ZaloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A REST controller that handles all API requests originating from the Zalo Mini App.
 */
@RestController
@RequestMapping("/api/zalo")
public class ZaloApiController {

    private final ZaloService zaloService;

    @Autowired
    public ZaloApiController(ZaloService zaloService) {
        this.zaloService = zaloService;
    }

    /**
     * API endpoint for logging in a user via their Zalo ID.
     * @param loginRequest DTO containing the Zalo User ID.
     * @return A ResponseEntity with the authenticated UserDto.
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> loginByZaloId(@RequestBody ZaloLoginRequestDto loginRequest) {
        UserDto userDto = zaloService.loginByZaloId(loginRequest.getZaloUserId());
        return ResponseEntity.ok(userDto);
    }

    /**
     * API endpoint for linking a Zalo account to a system (GSM) user account.
     * @param linkRequest DTO with Zalo ID, system username, and password.
     * @return A ResponseEntity with the linked UserDto.
     */
    @PostMapping("/link-account")
    public ResponseEntity<UserDto> linkAccount(@RequestBody ZaloLinkRequestDto linkRequest) {
        UserDto userDto = zaloService.linkAccount(linkRequest);
        return ResponseEntity.ok(userDto);
    }

    /**
     * API endpoint to get the distinct styles and colors for a given Sale Order.
     * Used to populate dropdowns in the Zalo Mini App.
     * @param saleOrderNo The Sale Order number.
     * @return A ResponseEntity with a DTO containing the sale order ID and a list of styles/colors.
     */
    @GetMapping("/sale-orders/{saleOrderNo}/styles")
    public ResponseEntity<ZaloSaleOrderInfoDto> getStylesAndColorsForSaleOrder(@PathVariable String saleOrderNo) {
        ZaloSaleOrderInfoDto info = zaloService.findStylesAndColorsBySaleOrderNo(saleOrderNo);
        return ResponseEntity.ok(info);
    }

    /**
     * API endpoint to save a batch of production output records from the Zalo Mini App.
     * @param outputDtos A list of production output data.
     * @return An empty ResponseEntity with a 200 OK status.
     */
    @PostMapping("/output")
    public ResponseEntity<Void> saveProductionOutputs(@RequestBody List<ProductionOutputDto> outputDtos) {
        zaloService.saveProductionOutputs(outputDtos);
        return ResponseEntity.ok().build();
    }
}