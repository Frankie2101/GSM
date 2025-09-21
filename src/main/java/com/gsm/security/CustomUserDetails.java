package com.gsm.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;


/**
 * A custom UserDetails implementation that extends Spring Security's default User class.
 * This is done to include additional, application-specific user information, such as the database ID.
 */
public class CustomUserDetails extends User {

    /**
     * The database primary key of the user.
     */
    private final Long userId;

    public CustomUserDetails(Long userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    /**
     * Provides access to the user's database ID.
     * @return The user's ID.
     */
    public Long getUserId() {
        return userId;
    }
}