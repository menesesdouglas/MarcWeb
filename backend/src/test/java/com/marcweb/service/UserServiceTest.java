package com.marcweb.service;

import com.marcweb.dto.user.CreateUser;
import com.marcweb.entity.Role;
import com.marcweb.entity.User;
import com.marcweb.exceptions.UsernameDuplicatedException;
import com.marcweb.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @InjectMocks
    private UserService userService;

    private CreateUser createUser;
    private User user;

    @BeforeEach
    void setUp() {
        createUser = new CreateUser();
        createUser.setNome("Test User");
        createUser.setUsername("testuser");
        createUser.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setNome("Test User");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(Role.EMPLOYEE);
    }

    @Test
    void createUser_DeveLancarExcecao_QuandoUsernameDuplicado() {
        // Arrange
        when(repository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(UsernameDuplicatedException.class, () ->
            userService.createUser(createUser)
        );
    }

    @Test
    void createUser_DeveCriarUsuarioComSucesso_QuandoUsernameUnico() throws Exception {
        // Arrange
        when(repository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenReturn(user);

        // Act
        userService.createUser(createUser);

        // Assert
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void findByUsername_DeveRetornarUsuario_QuandoExistir() {
        // Arrange
        when(repository.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.of(user));

        // Act
        User resultado = userService.findByUsername("testuser");

        // Assert
        assertNotNull(resultado);
        assertEquals("testuser", resultado.getUsername());
    }

    @Test
    void findByUsername_DeveLancarExcecao_QuandoNaoExistir() {
        // Arrange
        when(repository.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            userService.findByUsername("nonexistent")
        );
    }

    @Test
    void findById_DeveRetornarUsuario_QuandoExistir() {
        // Arrange
        when(repository.findById(anyLong())).thenReturn(Optional.of(user));

        // Act
        User resultado = userService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findById_DeveLancarExcecao_QuandoNaoExistir() {
        // Arrange
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            userService.findById(999L)
        );
    }

}
