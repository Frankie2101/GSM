// File: src/main/java/com/gsm/service/ZaloService.java
package com.gsm.service;

import com.gsm.dto.*;

import java.util.List;

/**
 * Defines the contract for services related to the Zalo Mini App integration.
 * This includes user authentication, account linking, and handling production output data.
 */
public interface ZaloService {

    /**
     * Authenticates a user via their Zalo User ID.
     * This is used when a user opens the Mini App and is already linked.
     * @param zaloUserId The unique identifier provided by Zalo for the user.
     * @return A UserDto containing essential user information if the link is found.
     */
    UserDto loginByZaloId(String zaloUserId);

    /**
     * Links a Zalo account to an existing system (GSM) user account.
     * Requires the user's system credentials for verification.
     * @param linkRequest A DTO containing the Zalo User ID, system username, and password.
     * @return A UserDto of the user whose account has been successfully linked.
     */
    UserDto linkAccount(ZaloLinkRequestDto linkRequest);


    /**
     * Finds all distinct style and color combinations for a given Sale Order number.
     * This is used to populate selection dropdowns in the Zalo Mini App.
     * @param saleOrderNo The business number of the Sale Order.
     * @return A DTO containing the Sale Order's internal ID and a list of its styles/colors.
     */
    ZaloSaleOrderInfoDto findStylesAndColorsBySaleOrderNo(String saleOrderNo);

    /**
     * Saves a batch of production output records submitted from the Zalo Mini App.
     * @param outputDtos A list of DTOs, each representing a single production output entry.
     */
    void saveProductionOutputs(List<ProductionOutputDto> outputDtos);
}