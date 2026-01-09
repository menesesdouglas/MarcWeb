package com.marcweb.service;

import com.marcweb.dto.CreateUser;
import com.marcweb.entity.Role;
import com.marcweb.entity.User;
import com.marcweb.exceptions.UsernameDuplicatedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.marcweb.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUser validUserRequest;
    private User existingUser;

    @BeforeEach
    void setup() {
        validUserRequest = new CreateUser();
        validUserRequest.setUsername("new.user");
        validUserRequest.setNome("New User");
        validUserRequest.setPassword("password123");
        validUserRequest.setRole(Role.EMPLOYEE);
        validUserRequest.setConsentGeoloc(false);

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existing.user");
        existingUser.setNome("Existing User");
        existingUser.setPassword("encoded_password");
        existingUser.setRole(Role.EMPLOYEE);
        existingUser.setAtivo(true);
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        assertDoesNotThrow(() -> userService.createUser(validUserRequest));

        verify(userRepository, times(1)).existsByUsername("new.user");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_DuplicateUsername() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(UsernameDuplicatedException.class,
            () -> userService.createUser(validUserRequest));

        verify(userRepository, times(1)).existsByUsername("new.user");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(existingUser));

        User foundUser = userService.findByUsername("existing.user");

        assertNotNull(foundUser);
        assertEquals("existing.user", foundUser.getUsername());
        assertEquals("Existing User", foundUser.getNome());

        verify(userRepository, times(1)).findByUsername("existing.user");
    }

    @Test
    void findByUsername_NotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> userService.findByUsername("nonexistent.user"));

        verify(userRepository, times(1)).findByUsername("nonexistent.user");
    }

    @Test
    void createUser_WithAdminRole() {
        CreateUser adminRequest = new CreateUser();
        adminRequest.setUsername("admin.user");
        adminRequest.setNome("Admin User");
        adminRequest.setPassword("admin123");
        adminRequest.setRole(Role.ADMIN);
        adminRequest.setConsentGeoloc(false);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_admin_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        assertDoesNotThrow(() -> userService.createUser(adminRequest));

        verify(userRepository, times(1)).existsByUsername("admin.user");
        verify(passwordEncoder, times(1)).encode("admin123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithConsentGeoloc() {
        CreateUser consentRequest = new CreateUser();
        consentRequest.setUsername("consent.user");
        consentRequest.setNome("Consent User");
        consentRequest.setPassword("password123");
        consentRequest.setRole(Role.EMPLOYEE);
        consentRequest.setConsentGeoloc(true);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        assertDoesNotThrow(() -> userService.createUser(consentRequest));

        verify(userRepository, times(1)).existsByUsername("consent.user");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }
}
