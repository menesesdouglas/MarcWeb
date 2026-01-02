package com.marcweb.service;

import com.marcweb.dto.CreateUser;
import com.marcweb.entity.User;
import com.marcweb.exceptions.UsernameDuplicatedException;
import com.marcweb.repository.UserRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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


    private void validateUsername(String username) {
        if (repository.existsByUsername(username)) {
            throw new UsernameDuplicatedException();
        }
    }
}
