package com.gsm.service;

import com.gsm.dto.*;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.*;
import com.gsm.repository.ProductionOutputRepository;
import com.gsm.repository.SaleOrderRepository;
import com.gsm.repository.UserRepository;
import com.gsm.service.ZaloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.util.UriComponentsBuilder; // Thêm import này

@Service
public class ZaloServiceImpl implements ZaloService {

    // Nâng cấp: Khai báo final và sử dụng Constructor Injection
    private final UserRepository userRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final ProductionOutputRepository productionOutputRepository;
    private final RestTemplate restTemplate;

    @Value("${zalo.app.id}")
    private String appId;

    @Value("${zalo.app.secret-key}")
    private String secretKey;

    @Value("${zalo.oauth.url}")
    private String zaloOauthUrl;

    @Value("${zalo.api.url}")
    private String zaloApiUrl;


    // --- CÁC BIẾN LƯU TRỮ TOKEN TRONG BỘ NHỚ ---
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiryTime;

    @Value("${zalo.app.access_token}")
    private String zaloAppAccessToken;

    @Autowired
    public ZaloServiceImpl(UserRepository userRepository, SaleOrderRepository saleOrderRepository,
                           ProductionOutputRepository productionOutputRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.productionOutputRepository = productionOutputRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto login(ZaloLoginRequestDto loginRequest) {
        try {
            String validAccessToken = this.getValidAccessToken();
            // SỬA ĐOẠN NÀY: Dùng UriComponentsBuilder để tạo URL với query params
            String urlWithParams = UriComponentsBuilder.fromHttpUrl(zaloApiUrl)
                    .queryParam("access_token", validAccessToken) // Dùng token động
                    .queryParam("code", loginRequest.getPhoneNumberToken()) // Tham số 2
                    .toUriString();

            // Log để debug: In ra URL hoàn chỉnh sắp gọi
            System.out.println("Calling Zalo API at: " + urlWithParams);

            // Không cần set headers cho các tham số này nữa
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ZaloUserResponse> response = restTemplate.exchange(
                    urlWithParams, // Dùng URL đã có tham số
                    HttpMethod.GET,
                    entity,
                    ZaloUserResponse.class
            );

            ZaloUserResponse zaloResponse = response.getBody();

            // Kiểm tra lỗi trả về từ Zalo (ví dụ: error code khác 0)
            if (zaloResponse == null || zaloResponse.getError() != 0 || zaloResponse.getPhone() == null || zaloResponse.getPhone().isEmpty()) {
                // In ra chi tiết lỗi từ Zalo nếu có
                System.err.println("Zalo API Response with error: " + zaloResponse);
                throw new SecurityException("Could not get phone number from Zalo. The token might have expired or is invalid.");
            }

            String phoneNumber = zaloResponse.getPhone();
            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Account with phone number " + phoneNumber + " is not registered."));

            if (!user.isActiveFlag()) {
                throw new SecurityException("Your account has been disabled.");
            }

            UserDto userDto = new UserDto();
            userDto.setUserId(user.getUserId());
            userDto.setUserName(user.getUserName());
            userDto.setDepartment(user.getDepartment());
            userDto.setProductionLine(user.getProductionLine());
            userDto.setUserType(user.getUserType());
            userDto.setActiveFlag(user.isActiveFlag());
            return userDto;

        } catch (HttpClientErrorException e) {
            // In ra lỗi chi tiết HTTP (ví dụ: 401 Unauthorized, 403 Forbidden)
            System.err.println("HTTP Error from Zalo API: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new SecurityException("Failed to call Zalo API. Check server console for details.");
        }
    }

    private synchronized String getValidAccessToken() {
        if (accessToken == null || expiryTime == null || expiryTime.isBefore(LocalDateTime.now().plusMinutes(5))) {
            System.out.println("Access token is invalid or expiring. Refreshing...");
            refreshAccessToken();
        }
        return accessToken;
    }

    private void refreshAccessToken() {
        if (this.refreshToken == null) {
            throw new IllegalStateException("Refresh token is missing. Please get initial token first.");
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
                System.out.println("Successfully refreshed Zalo token. New expiry time: " + this.expiryTime);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not refresh Zalo access token.", e);
        }
    }

    public void getInitialTokens(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("secret_key", this.secretKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("app_id", this.appId);
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<ZaloOAuthResponse> response = restTemplate.postForEntity(zaloOauthUrl, requestEntity, ZaloOAuthResponse.class);
            ZaloOAuthResponse tokenResponse = response.getBody();
            if (tokenResponse != null) {
                this.accessToken = tokenResponse.getAccessToken();
                this.refreshToken = tokenResponse.getRefreshToken();
                this.expiryTime = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn() - 60);

                System.out.println("--- !!! IMPORTANT !!! ---");
                System.out.println("Obtained initial tokens successfully. PLEASE SAVE THIS REFRESH TOKEN:");
                System.out.println(this.refreshToken);
                System.out.println("-------------------------");
            }
        } catch (Exception e) {
            System.err.println("Failed to get initial Zalo token: " + e.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<ZaloSaleOrderDetailDto> getSaleOrderDetailsForZalo(String saleOrderNo) {
        SaleOrder saleOrder = saleOrderRepository.findBySaleOrderNo(saleOrderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with code: " + saleOrderNo));

        // ... (phần còn lại giữ nguyên)
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
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        // ... (phần còn lại giữ nguyên)
        if (outputDtos == null || outputDtos.isEmpty()){ return; }
        Long saleOrderId = outputDtos.get(0).getSaleOrderId();
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found with ID: " + saleOrderId));
        List<ProductionOutput> outputsToSave = new ArrayList<>();
        for (ProductionOutputDto dto : outputDtos) {
            if (dto.getOutputQuantity() == null || dto.getOutputQuantity() <= 0) { continue; }
            ProductionOutput newOutput = new ProductionOutput();
            newOutput.setSaleOrder(saleOrder);
            newOutput.setStyle(dto.getStyle());
            newOutput.setColor(dto.getColor());
            newOutput.setOutputQuantity(dto.getOutputQuantity());
            newOutput.setOutputDate(LocalDate.now());
            newOutput.setDepartment(currentUser.getDepartment());
            newOutput.setProductionLine(currentUser.getProductionLine());
            outputsToSave.add(newOutput);
        }
        productionOutputRepository.saveAll(outputsToSave);
    }
}