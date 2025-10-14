package com.gsm.service;

import com.gsm.dto.UserDto;
import com.gsm.enums.UserType;
import com.gsm.exception.DuplicateResourceException;
import com.gsm.exception.ResourceNotFoundException;
import com.gsm.model.User;
import com.gsm.repository.UserRepository;
import com.gsm.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The concrete implementation of the UserService interface.
 * Contains all business logic for user management and integrates with Spring Security.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /**
     * Saves a user. This handles both creation of new users and updates to existing ones.
     * It includes logic to check for duplicate phone numbers and to hash the password.
     */
    @Override
    @Transactional
    public UserDto save(UserDto dto) {
        userRepository.findByPhoneNumber(dto.getPhoneNumber()).ifPresent(existing -> {
            if (dto.getUserId() == null || !existing.getUserId().equals(dto.getUserId())) {
                throw new DuplicateResourceException("Phone number '" + dto.getPhoneNumber() + "' already exists.");
            }
        });

        User user = (dto.getUserId() != null)
                ? userRepository.findById(dto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"))
                : new User();

        mapDtoToEntity(dto, user);
        User savedUser = userRepository.save(user);
        return convertEntityToDto(savedUser);
    }

    @Override
    @Transactional
    public void setActiveFlag(Long id, boolean status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActiveFlag(status);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> search(String keyword) {
        String effectiveKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<User> users = userRepository.search(effectiveKeyword);
        AtomicInteger index = new AtomicInteger(1);
        return users.stream().map(user -> {
            UserDto dto = convertEntityToDto(user);
            dto.setSequenceNumber((long) index.getAndIncrement());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertEntityToDto(user);
    }

    /**
     * Maps the data from a UserDto to a User entity.
     * If a new password is provided in the DTO, it will be encoded before being set.
     */
    private void mapDtoToEntity(UserDto dto, User user) {
        user.setUserName(dto.getUserName());
        user.setDepartment(dto.getDepartment());
        user.setProductionLine(dto.getProductionLine());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setUserType(dto.getUserType());
        user.setEmailAddress(dto.getEmailAddress());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // If the user is an Admin, their permissions list should be empty.
        if (dto.getUserType() == UserType.Admin) {
            user.getPermissions().clear();
        } else {
            // For Normal users, update their permissions from the DTO.
            user.getPermissions().clear();
            if (dto.getPermissions() != null) {
                user.getPermissions().addAll(dto.getPermissions());
            }
        }

    }

    private UserDto convertEntityToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setDepartment(user.getDepartment());
        dto.setProductionLine(user.getProductionLine());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setUserType(user.getUserType());
        dto.setEmailAddress(user.getEmailAddress());
        dto.setActiveFlag(user.isActiveFlag());

        dto.setPermissions(user.getPermissions());

        return dto;
    }

    /**
     * This method is required by Spring Security's UserDetailsService.
     * It loads a user by their username and returns a UserDetails object.
     * We return our CustomUserDetails to include the user's database ID and their granted authorities.
     * @param username The username to look for.
     * @return A CustomUserDetails object for Spring Security.
     * @throws UsernameNotFoundException if the user is not found.
     * @throws DisabledException if the user's account is not active.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.gsm.model.User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (!user.isActiveFlag()) {
            throw new DisabledException("User account is disabled.");
        }

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Step 1: Log the exact value read from the database for diagnostics.
        if (user.getUserType() != null && "Admin".equalsIgnoreCase(user.getUserType().name())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_Admin"));
        } else {
            user.getPermissions().forEach(permission -> {
                authorities.add(new SimpleGrantedAuthority(permission));
            });
        }

        // Return the CustomUserDetails object with the correct set of authorities.
        return new CustomUserDetails(
                user.getUserId(),
                user.getUserName(),
                user.getPassword(),
                authorities
        );
    }
}