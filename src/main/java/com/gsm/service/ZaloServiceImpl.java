package com.gsm.service;

import com.gsm.dto.*;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.ProductionOutput;
import com.gsm.model.SaleOrder;
import com.gsm.model.User;
import com.gsm.repository.ProductionOutputRepository;
import com.gsm.repository.SaleOrderRepository;
import com.gsm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ZaloServiceImpl implements ZaloService {

    private static final Logger log = LoggerFactory.getLogger(ZaloServiceImpl.class);

    private final UserRepository userRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final ProductionOutputRepository productionOutputRepository;

    public ZaloServiceImpl(UserRepository userRepository, SaleOrderRepository saleOrderRepository,
                           ProductionOutputRepository productionOutputRepository) {
        this.userRepository = userRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.productionOutputRepository = productionOutputRepository;
    }

    // --- CÁC PHƯƠNG THỨC MỚI VÀ ĐƯỢC CẬP NHẬT ---

    @Override
    @Transactional(readOnly = true)
    public UserDto findUserByUserName(String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userName));
        return mapUserToDto(user);
    }

    @Override
    @Transactional
    public void saveProductionOutputs(List<ProductionOutputDto> outputDtos) {
        if (outputDtos == null || outputDtos.isEmpty()) {
            log.warn("Received an empty or null list of outputs to save. Aborting.");
            return;
        }

        // Lấy thông tin chung từ bản ghi đầu tiên trong danh sách
        ProductionOutputDto firstDto = outputDtos.get(0);
        Long saleOrderId = firstDto.getSaleOrderId();
        String department = firstDto.getDepartment();
        String productionLine = firstDto.getProductionLine();

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

                    // [LOGIC MỚI] Lấy thông tin trạm từ DTO thay vì từ user
                    newOutput.setDepartment(department);
                    newOutput.setProductionLine(productionLine);

                    return newOutput;
                })
                .collect(Collectors.toList());

        if (outputsToSave.isEmpty()) {
            log.info("No valid output quantities to save for Sale Order ID {}", saleOrderId);
            return;
        }

        productionOutputRepository.saveAll(outputsToSave);
        log.info("Saved {} production output records for Sale Order ID {} from station [{}-{}]",
                outputsToSave.size(), saleOrderId, department, productionLine);
    }

    // --- CÁC PHƯƠNG THỨC CŨ HƠN ---

    @Override
    @Transactional(readOnly = true)
    public List<ZaloSaleOrderDetailDto> getSaleOrderDetailsForZalo(String saleOrderNo) {
        SaleOrder saleOrder = saleOrderRepository.findBySaleOrderNo(saleOrderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with code: " + saleOrderNo));

        // Logic này đã tốt, không cần thay đổi
        return saleOrder.getDetails().stream()
                .collect(Collectors.groupingBy(
                        detail -> detail.getProductVariant().getProduct().getProductCode() + "||" + detail.getProductVariant().getColor()
                ))
                .entrySet().stream()
                .map(entry -> {
                    ZaloSaleOrderDetailDto groupDto = new ZaloSaleOrderDetailDto();
                    String[] keys = entry.getKey().split("\\|\\|");
                    groupDto.setStyle(keys[0]);
                    groupDto.setColor(keys[1]);
                    int totalOrder = entry.getValue().stream().mapToInt(d -> d.getOrderQuantity()).sum();
                    int totalShipped = entry.getValue().stream().mapToInt(d -> d.getShipQuantity() == null ? 0 : d.getShipQuantity()).sum();
                    groupDto.setTotalOrderQty(totalOrder);
                    groupDto.setTotalShippedQty(totalShipped);
                    return groupDto;
                })
                .collect(Collectors.toList());
    }

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
