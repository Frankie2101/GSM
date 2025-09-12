// File: src/main/java/com/gsm/service/ZaloServiceImpl.java
package com.gsm.service;

import com.gsm.dto.*;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ZaloServiceImpl implements ZaloService {

    private static final Logger log = LoggerFactory.getLogger(ZaloServiceImpl.class);

    // Repositories
    private final UserRepository userRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final SaleOrderDetailRepository saleOrderDetailRepository;
    private final ProductionOutputRepository productionOutputRepository;
    private final RestTemplate restTemplate;

    // Zalo Config
    @Value("${zalo.app.id}") private String appId;
    @Value("${zalo.app.secret-key}") private String secretKey;
    @Value("${zalo.oauth.url}") private String zaloOauthUrl;
    @Value("${zalo.api.url}") private String zaloApiUrl;

    // Zalo Token Management
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiryTime;

    @Autowired
    public ZaloServiceImpl(UserRepository userRepository, SaleOrderRepository saleOrderRepository, SaleOrderDetailRepository saleOrderDetailRepository, ProductionOutputRepository productionOutputRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.saleOrderDetailRepository = saleOrderDetailRepository;
        this.productionOutputRepository = productionOutputRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto login(ZaloLoginRequestDto loginRequest) {
        // Step 1: Get phone number from Zalo using the phone token
        String validAppAccessToken = this.getValidAccessToken();
        String urlWithParams = UriComponentsBuilder.fromHttpUrl(zaloApiUrl)
                .queryParam("access_token", validAppAccessToken)
                .queryParam("code", loginRequest.getPhoneNumberToken())
                .toUriString();

        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<ZaloUserResponseDto> response = restTemplate.exchange(urlWithParams, HttpMethod.GET, entity, ZaloUserResponseDto.class);

        ZaloUserResponseDto zaloResponse = response.getBody();
        if (zaloResponse == null || zaloResponse.getError() != 0 || zaloResponse.getPhone() == null) {
            log.error("Failed to get phone number from Zalo: {}", zaloResponse);
            throw new SecurityException("Could not get phone number from Zalo. The token might be invalid.");
        }

        // Step 2: Find user in GSM database by phone number
        String phoneNumber = zaloResponse.getPhone();
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User with phone number " + phoneNumber + " is not registered in GSM system."));

        return mapUserToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZaloStyleColorDto> findStylesAndColorsBySaleOrderNo(String saleOrderNo) {
        SaleOrder saleOrder = saleOrderRepository.findBySaleOrderNo(saleOrderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with number: " + saleOrderNo));

        List<Map<String, String>> results = saleOrderDetailRepository.findDistinctStylesAndColorsBySaleOrderId(saleOrder.getSaleOrderId());

        return results.stream()
                .map(result -> new ZaloStyleColorDto(result.get("style"), result.get("color")))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveProductionOutputs(List<ProductionOutputDto> outputDtos) {
        if (outputDtos == null || outputDtos.isEmpty()) {
            log.warn("Received an empty or null list of outputs to save. Aborting.");
            return;
        }

        // Lấy userId từ DTO đầu tiên (giả định tất cả đến từ cùng 1 user trong 1 lần submit)
        Long creatorUserId = outputDtos.get(0).getUserId();
        if (creatorUserId == null) {
            throw new IllegalArgumentException("User ID is required to save production output.");
        }

        // [LOGIC MỚI] Dùng userId để lấy thông tin User đầy đủ và chính xác từ DB
        User user = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + creatorUserId));

        // Lấy saleOrderId từ DTO
        Long saleOrderId = outputDtos.get(0).getSaleOrderId();
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with ID: " + saleOrderId));

        List<ProductionOutput> outputsToSave = outputDtos.stream()
                .filter(dto -> dto.getOutputQuantity() != null && dto.getOutputQuantity() > 0)
                .map(dto -> {
                    // Đổi tên biến: Tạo một entity để lưu
                    ProductionOutput productionOutputEntity = new ProductionOutput();
                    productionOutputEntity.setSaleOrder(saleOrder);
                    productionOutputEntity.setStyle(dto.getStyle());
                    productionOutputEntity.setColor(dto.getColor());
                    productionOutputEntity.setOutputQuantity(dto.getOutputQuantity());
                    productionOutputEntity.setOutputDate(LocalDate.now());

                    // [LOGIC MỚI] Lấy department và line từ User đã truy vấn, an toàn hơn
                    productionOutputEntity.setDepartment(user.getDepartment());
                    productionOutputEntity.setProductionLine(user.getProductionLine());

                    // Gán người tạo thủ công
                    productionOutputEntity.setCreatedBy(user.getUserId());

                    return productionOutputEntity;
                })
                .collect(Collectors.toList());

        if (!outputsToSave.isEmpty()) {
            productionOutputRepository.saveAll(outputsToSave);
            log.info("Saved {} production output records for Sale Order ID {} by user '{}'",
                    outputsToSave.size(), saleOrderId, user.getUserName());
        }
    }
    // --- Helper and Token Management Methods ---

    private UserDto mapUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setDepartment(user.getDepartment());
        dto.setProductionLine(user.getProductionLine());
        dto.setUserType(user.getUserType());
        dto.setActiveFlag(user.isActiveFlag());
        return dto;
    }

    private synchronized String getValidAccessToken() {
        if (accessToken == null || expiryTime == null || expiryTime.isBefore(LocalDateTime.now().plusMinutes(5))) {
            refreshAccessToken();
        }
        return accessToken;
    }

    private void refreshAccessToken() {
        if (this.refreshToken == null) {
            throw new IllegalStateException("Refresh token is missing. Please perform the one-time manual authorization to get the initial token.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("secret_key", this.secretKey);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("app_id", this.appId);
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", this.refreshToken);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<ZaloOAuthResponse> response = restTemplate.postForEntity(zaloOauthUrl, requestEntity, ZaloOAuthResponse.class);
            ZaloOAuthResponse tokenResponse = response.getBody();
            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                this.accessToken = tokenResponse.getAccessToken();
                this.refreshToken = tokenResponse.getRefreshToken();
                this.expiryTime = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn() - 60);
                log.info("Successfully refreshed Zalo app access token.");
            }
        } catch (Exception e) {
            log.error("Could not refresh Zalo access token.", e);
            throw new RuntimeException("Could not refresh Zalo access token.", e);
        }
    }
}