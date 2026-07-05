package com.company.dms.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityConfigTest {

    @Test
    void userDetailsServiceDoesNotProvideDefaultUser() {
        UserDetailsService service = new SecurityConfig().userDetailsService();

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("user"));
    }
}