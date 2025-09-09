package com.gsm.service;

import com.gsm.dto.*;
import com.gsm.dto.LoginStatus;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.ProductionOutput;
import com.gsm.model.SaleOrder;
import com.gsm.model.SaleOrderDetail;
import com.gsm.model.User;
import com.gsm.repository.ProductionOutputRepository;
import com.gsm.repository.SaleOrderRepository;
import com.gsm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ZaloServiceImpl implements ZaloService {

    private static final Logger log = LoggerFactory.getLogger(ZaloServiceImpl.class);

    private final UserRepository userRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final ProductionOutputRepository productionOutputRepository;
    // Ghi chú: ZaloAuthService có thể được inject vào đây nếu các phương thức khác cần đến
    // private final ZaloAuthService zaloAuthService;

    public ZaloServiceImpl(UserRepository userRepository, SaleOrderRepository saleOrderRepository,
                           ProductionOutputRepository productionOutputRepository) {
        this.userRepository = userRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.productionOutputRepository = productionOutputRepository;
    }

    @Override
    @Transactional
    public ZaloLoginResponseDto login(ZaloLoginRequestDto loginRequest) {
        log.info("Attempting to log in user with Zalo ID: {}", loginRequest.getZaloUserId());
        Optional<User> userOptional = userRepository.findByZaloUserId(loginRequest.getZaloUserId());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.isActiveFlag()) {
                log.warn("Login failed for Zalo ID {}: Account is disabled.", user.getZaloUserId());
                throw new SecurityException("Your account has been disabled.");
            }
            log.info("Zalo user {} found. Login successful.", user.getZaloUserId());
            UserDto userDto = mapUserToDto(user);
            return new ZaloLoginResponseDto(LoginStatus.SUCCESS, userDto, null, null);
        } else {
            log.info("Zalo ID {} not found. Account linking is required.", loginRequest.getZaloUserId());
            return new ZaloLoginResponseDto(LoginStatus.LINK_REQUIRED, null, loginRequest.getZaloUserId(), loginRequest.getUserName());
        }
    }

    @Override
    @Transactional
    public UserDto linkAccount(ZaloLinkRequestDto linkRequest) {
        log.info("Attempting to link Zalo ID {} to phone number {}", linkRequest.getZaloUserId(), linkRequest.getPhoneNumber());
        User user = userRepository.findByPhoneNumber(linkRequest.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Phone number not found in the system."));

        if (user.getZaloUserId() != null && !user.getZaloUserId().isEmpty()) {
            log.warn("Failed to link Zalo ID {}: GSM account is already linked to Zalo ID {}", linkRequest.getZaloUserId(), user.getZaloUserId());
            throw new IllegalStateException("This GSM account has already been linked to another Zalo account.");
        }

        user.setZaloUserId(linkRequest.getZaloUserId());
        userRepository.save(user);
        log.info("Successfully linked Zalo ID {} to user {}", user.getZaloUserId(), user.getUserId());

        return mapUserToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZaloSaleOrderDetailDto> getSaleOrderDetailsForZalo(String saleOrderNo) {
        SaleOrder saleOrder = saleOrderRepository.findBySaleOrderNo(saleOrderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with code: " + saleOrderNo));

        Map<String, List<SaleOrderDetail>> groupedByStyleAndColor = saleOrder.getDetails().stream()
                .collect(Collectors.groupingBy(detail ->
                        detail.getProductVariant().getProduct().getProductCode() + "||" + detail.getProductVariant().getColor()
                ));

        List<ZaloSaleOrderDetailDto> result = new ArrayList<>();
        for (Map.Entry<String, List<SaleOrderDetail>> entry : groupedByStyleAndColor.entrySet()) {
            ZaloSaleOrderDetailDto groupDto = new ZaloSaleOrderDetailDto();
            String[] keys = entry.getKey().split("\\|\\|");
            groupDto.setStyle(keys[0]);
            groupDto.setColor(keys[1]);
            int totalOrder = entry.getValue().stream().mapToInt(SaleOrderDetail::getOrderQuantity).sum();
            int totalShipped = entry.getValue().stream().mapToInt(d -> d.getShipQuantity() == null ? 0 : d.getShipQuantity()).sum();
            groupDto.setTotalOrderQty(totalOrder);
            groupDto.setTotalShippedQty(totalShipped);
            result.add(groupDto);
        }
        return result;
    }

    @Override
    @Transactional
    public void saveProductionOutputs(List<ProductionOutputDto> outputDtos, Long userId) {
        if (outputDtos == null || outputDtos.isEmpty()) {
            return;
        }
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Long saleOrderId = outputDtos.get(0).getSaleOrderId();
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with ID: " + saleOrderId));

        List<ProductionOutput> outputsToSave = outputDtos.stream()
                .filter(dto -> dto.getOutputQuantity() != null && dto.getOutputQuantity() > 0)
                .map(dto -> {
                    ProductionOutput newOutput = new ProductionOutput();
                    newOutput.setSaleOrder(saleOrder);
                    newOutput.setStyle(dto.getStyle());
                    newOutput.setColor(dto.getColor());
                    newOutput.setOutputQuantity(dto.getOutputQuantity());
                    newOutput.setOutputDate(LocalDate.now());
                    newOutput.setDepartment(currentUser.getDepartment());
                    newOutput.setProductionLine(currentUser.getProductionLine());
                    return newOutput;
                })
                .collect(Collectors.toList());

        productionOutputRepository.saveAll(outputsToSave);
        log.info("Saved {} production output records for user ID {}", outputsToSave.size(), userId);
    }

    /**
     * Maps a User entity to a UserDto.
     *
     * @param user The User entity to map.
     * @return The resulting UserDto.
     */
    private UserDto mapUserToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setUserName(user.getUserName());
        userDto.setDepartment(user.getDepartment());
        userDto.setProductionLine(user.getProductionLine());
        userDto.setUserType(user.getUserType());
        userDto.setActiveFlag(user.isActiveFlag());
        return userDto;
    }
}