package com.gsm.service;

import com.gsm.dto.UserDto;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Defines the contract for user management business operations.
 * It also extends UserDetailsService to integrate with Spring Security.
 */
public interface UserService extends UserDetailsService {
    List<UserDto> search(String keyword);
    UserDto findById(Long id);
    UserDto save(UserDto userDto);
    void setActiveFlag(Long id, boolean status);
}