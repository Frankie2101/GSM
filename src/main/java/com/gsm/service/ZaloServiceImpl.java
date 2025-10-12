package com.gsm.service;

import com.gsm.dto.ProductionOutputDto;
import com.gsm.dto.UserDto;
import com.gsm.dto.ZaloLinkRequestDto;
import com.gsm.dto.ZaloStyleColorDto;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.ProductionOutput;
import com.gsm.model.SaleOrder;
import com.gsm.model.User;
import com.gsm.repository.ProductionOutputRepository;
import com.gsm.repository.SaleOrderDetailRepository;
import com.gsm.repository.SaleOrderRepository;
import com.gsm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.gsm.dto.ZaloSaleOrderInfoDto;

/**
 * The concrete implementation of the ZaloService interface.
 * Handles all business logic related to Zalo Mini App integration.
 */
@Service
public class ZaloServiceImpl implements ZaloService {

    private static final Logger log = LoggerFactory.getLogger(ZaloServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SaleOrderRepository saleOrderRepository;
    private final SaleOrderDetailRepository saleOrderDetailRepository;
    private final ProductionOutputRepository productionOutputRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor-based dependency injection.
     */
    @Autowired
    public ZaloServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, SaleOrderRepository saleOrderRepository, SaleOrderDetailRepository saleOrderDetailRepository, ProductionOutputRepository productionOutputRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.saleOrderRepository = saleOrderRepository;
        this.saleOrderDetailRepository = saleOrderDetailRepository;
        this.productionOutputRepository = productionOutputRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Attempts to log in a user using their Zalo ID.
     * Contains extensive logging and a direct JDBC query for debugging potential issues with the JPA layer.
     * @param zaloUserId The user's unique Zalo identifier.
     * @return A UserDto if the user is found and linked.
     * @throws ResourceNotFoundException if no account is linked to the Zalo ID.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto loginByZaloId(String zaloUserId) {
        log.info("--- Starting loginByZaloId ---");
        log.info("Attempting to find user with Zalo User ID: [{}]", zaloUserId);

        // Use JDBC to query the DB directly for verification.
        try {
            String sql = "SELECT UserId, UserName, zaloUserId FROM Users WHERE zaloUserId = ?";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, zaloUserId);

            log.info("Direct JDBC query found {} user(s) with this Zalo ID.", users.size());

            if (!users.isEmpty()) {
                users.forEach(user -> log.info("Found user via JDBC: {}", user.toString()));
            }
        } catch (Exception e) {
            log.error("Error during direct JDBC query test", e);
        }

        log.info("Now attempting to find user with Spring Data JPA...");
        try {
            User user = userRepository.findByZaloUserId(zaloUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Zalo user ID not linked to any account: " + zaloUserId));

            log.info("Successfully found unique user via Spring Data JPA: {}", user.getUserName());
            return mapUserToDto(user);

        } catch (Exception e) {
            log.error("!!! EXCEPTION CAUGHT during userRepository.findByZaloUserId !!!", e);
            throw e;
        }
    }

    /**
     * Links a Zalo account to a system user account after validating credentials.
     * @param linkRequest DTO with Zalo ID, username, and password.
     * @return A UserDto of the linked account.
     * @throws ResourceNotFoundException if the username does not exist.
     * @throws IllegalStateException if the Zalo account is already linked to another user.
     * @throws SecurityException if the password is incorrect.
     */
    @Override
    @Transactional
    public UserDto linkAccount(ZaloLinkRequestDto linkRequest) {
        User user = userRepository.findByUserName(linkRequest.getUserName())
                .orElseThrow(() -> new ResourceNotFoundException("GSM User not found: " + linkRequest.getUserName()));

        userRepository.findByZaloUserId(linkRequest.getZaloUserId()).ifPresent(existingUser -> {
            if (!existingUser.getUserId().equals(user.getUserId())) {
                throw new IllegalStateException("This Zalo account is already linked to another user.");
            }
        });

        if (!passwordEncoder.matches(linkRequest.getPassword(), user.getPassword())) {
            throw new SecurityException("Invalid credentials.");
        }

        user.setZaloUserId(linkRequest.getZaloUserId());
        userRepository.save(user);
        return mapUserToDto(user);
    }

    /**
     * Finds distinct style/color combinations for a given Sale Order number.
     * @param saleOrderNo The business number of the Sale Order.
     * @return A DTO containing the sale order's ID and the list of styles/colors.
     */
    @Override
    @Transactional(readOnly = true)
    public ZaloSaleOrderInfoDto findStylesAndColorsBySaleOrderNo(String saleOrderNo) {
        SaleOrder saleOrder = saleOrderRepository.findBySaleOrderNo(saleOrderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with number: " + saleOrderNo));

        List<Map<String, String>> results = saleOrderDetailRepository.findDistinctStylesAndColorsBySaleOrderId(saleOrder.getSaleOrderId());

        List<ZaloStyleColorDto> styles = results.stream()
                .map(result -> new ZaloStyleColorDto(result.get("style"), result.get("color")))
                .collect(Collectors.toList());

        return new ZaloSaleOrderInfoDto(saleOrder.getSaleOrderId(), styles);
    }

    /**
     * Saves a list of production output records.
     * It validates the input, finds the associated user and sale order, maps DTOs to entities,
     * and performs a batch save operation.
     * @param outputDtos The list of production output data to save.
     */
    @Override
    @Transactional
    public void saveProductionOutputs(List<ProductionOutputDto> outputDtos) {
        if (outputDtos == null || outputDtos.isEmpty()) {
            log.warn("Received an empty or null list of outputs to save. Aborting.");
            return;
        }

        Long creatorUserId = outputDtos.get(0).getUserId();
        if (creatorUserId == null) {
            throw new IllegalArgumentException("User ID is required to save production output.");
        }

        User user = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + creatorUserId));

        Long saleOrderId = outputDtos.get(0).getSaleOrderId();
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with ID: " + saleOrderId));

        List<ProductionOutput> outputsToSave = outputDtos.stream()
                .filter(dto -> dto.getOutputQuantity() != null && dto.getOutputQuantity() > 0)
                .map(dto -> {
                    ProductionOutput productionOutputEntity = new ProductionOutput();
                    productionOutputEntity.setSaleOrder(saleOrder);
                    productionOutputEntity.setStyle(dto.getStyle());
                    productionOutputEntity.setColor(dto.getColor());
                    productionOutputEntity.setOutputQuantity(dto.getOutputQuantity());
                    productionOutputEntity.setOutputDate(LocalDate.now());
                    productionOutputEntity.setDepartment(user.getDepartment());
                    productionOutputEntity.setProductionLine(user.getProductionLine());
                    productionOutputEntity.setCreatedBy(user.getUserId());
                    productionOutputEntity.setLastModifiedBy(user.getUserId());
                    return productionOutputEntity;
                })
                .collect(Collectors.toList());

        if (!outputsToSave.isEmpty()) {
            productionOutputRepository.saveAll(outputsToSave);
            log.info("Saved {} production output records for Sale Order ID {} by user '{}'",
                    outputsToSave.size(), saleOrderId, user.getUserName());
        }
    }

    /**
     * A private helper method to map a User entity to a UserDto.
     * This ensures that only necessary, non-sensitive data is exposed.
     * @param user The User entity to map.
     * @return The resulting UserDto.
     */
    private UserDto mapUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setDepartment(user.getDepartment());
        dto.setProductionLine(user.getProductionLine());
        return dto;
    }
}