package com.marcweb.service;

import com.marcweb.dto.CreateUser;
import com.marcweb.entity.User;
import com.marcweb.exceptions.UsernameDuplicatedException;
import com.marcweb.repository.UserRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import com.marcweb.dto.UserResponse; // Import necessário
import com.marcweb.repository.UserRepository; // Import necessário, se ainda não estiver

import java.util.stream.Collectors;
import java.util.List;

import org.springframework.data.domain.Page; // Import para paginação (opcional)
import org.springframework.data.domain.Pageable; // Import para paginação (opcional)

@RequiredArgsConstructor
@Service
public class UserService {


    private final UserRepository repository;

    private final PasswordEncoder encoder;

    public void createUser(@RequestBody @Valid CreateUser data) {
        validateUsername(data.getUsername());
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


    @Transactional
    public void editUser(@PathVariable Long id, @RequestBody CreateUser data) {
        var user = repository.findById(id).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        if (!user.getUsername().equals(data.getUsername())) validateUsername(data.getUsername());
        if (StringUtils.isNotBlank(data.getUsername())) user.setUsername(data.getUsername());
        if (StringUtils.isNotBlank(data.getRole().name())) user.setRole(data.getRole());
        if (StringUtils.isNotBlank(data.getNome())) user.setNome(data.getNome());
        if (StringUtils.isNotBlank(data.getPassword())) user.setPassword(encoder.encode(data.getPassword()));
        if (data.getConsentGeoloc() != null) user.setConsentGeoloc(data.getConsentGeoloc());
    }

    public void deleteUser(@PathVariable Long id) {
        repository.deleteById(id);
    }

    public User findByUsername(String username) {
        return repository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }


    private void validateUsername(String username) {
        if (repository.existsByUsername(username)) {
            throw new UsernameDuplicatedException();
        }
    }

    public static UserDetails getUserDetailsBySecurityContext() {
        return (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public List<UserResponse> findAll() {
        return repository.findAll().stream()
                .map(UserResponse::new) // Converte cada User em UserResponse
                .collect(Collectors.toList());
    }

    public Page<UserResponse> findAll(int pageNumber) {
        Pageable page = PageRequest.of(pageNumber, 20);
        return repository.findAll(page)
                .map(UserResponse::new); // Converte cada User da página em UserResponse
    }


    public User findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }


}
