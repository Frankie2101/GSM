package com.gsm.controller.api;

import com.gsm.dto.UserDto;
import com.gsm.security.CustomUserDetails;
import com.gsm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @Autowired private UserService userService;

    @GetMapping
    public List<UserDto> searchUsers(@RequestParam(required = false) String keyword) {
        return userService.search(keyword);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.findById(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            // Trả về lỗi 401 Unauthorized nếu không có thông tin user
            return ResponseEntity.status(401).build();
        }
        UserDto userDto = userService.findById(userDetails.getUserId());
        return ResponseEntity.ok(userDto);
    }

    @PostMapping
    public ResponseEntity<UserDto> saveUser(@RequestBody UserDto userDto) {
        UserDto savedUser = userService.save(userDto);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.setActiveFlag(id, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.setActiveFlag(id, true);
        return ResponseEntity.ok().build();
    }
}