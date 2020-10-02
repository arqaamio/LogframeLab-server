package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.BaseControllerTest;
import com.arqaam.logframelab.exception.UserNotFoundException;
import com.arqaam.logframelab.model.persistence.auth.User;
import com.arqaam.logframelab.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest extends BaseControllerTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername() {
        String username = "username";
        User userDetails = new User();
        when(userRepository.findByUsernameWithGroupMemberships(username)).thenReturn(Optional.of(userDetails));
        UserDetails result = userDetailsService.loadUserByUsername(username);
        assertEquals(userDetails, result);
    }

    @Test
    void loadUserByUsername_userNotFound() {
        String username = "username";
        when(userRepository.findByUsernameWithGroupMemberships(username)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, ()->userDetailsService.loadUserByUsername(username));
    }
}