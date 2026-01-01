package com.marcweb.controller;

import com.marcweb.dto.CreateUser;
import com.marcweb.entity.User;
import com.marcweb.exceptions.UsernameDuplicatedException;
import com.marcweb.repository.UserRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // INJETANDO AQUI ANTES DE CRIAR O SERVICE
    private final UserRepository repository;

    private final PasswordEncoder encoder;

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createUser(@RequestBody @Valid CreateUser data) {
        validarUsername(data.getUsername());
        Boolean consent = data.getConsentGeoloc();
        User user = User.builder()
                .ativo(true)
                .nome(data.getNome())
                .username(data.getUsername())
                .role(data.getRole())
                .password(encoder.encode(data.getPassword()))
                .consentGeoloc(consent != null ? consent : false)
                .build();
        repository.save(user);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    public ResponseEntity<String> editUser(@PathVariable Long id, @RequestBody CreateUser data) {
        var user = repository.findById(id).orElse(null);
        if (user == null) {
            log.info("Usuario nao encontrado");
            return ResponseEntity.notFound().build();
        }
        if (!user.getUsername().equals(data.getUsername())) validarUsername(data.getUsername());
        if (StringUtils.isNotBlank(data.getUsername())) user.setUsername(data.getUsername());
        if (StringUtils.isNotBlank(data.getRole().name())) user.setRole(data.getRole());
        if (StringUtils.isNotBlank(data.getNome())) user.setNome(data.getNome());
        if (StringUtils.isNotBlank(data.getPassword())) user.setPassword(encoder.encode(data.getPassword()));
        if (data.getConsentGeoloc() != null) user.setConsentGeoloc(data.getConsentGeoloc());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private void validarUsername(String username) {
        if (repository.existsByUsername(username)) {
            log.warn("Username informado ja esta sendo utilizado");
            throw new UsernameDuplicatedException();
        }
    }

}
