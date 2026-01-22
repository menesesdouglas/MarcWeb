package com.marcweb.service;

import com.marcweb.auditoria.Auditado;
import com.marcweb.auditoria.Detalhes;
import com.marcweb.dto.*;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.User;
import com.marcweb.exceptions.UsernameDuplicatedException;
import com.marcweb.repository.UserRepository;
import com.marcweb.security.JwtService;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import com.marcweb.repository.UserRepository; // Import necessário, se ainda não estiver

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {


    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final Detalhes detalhes;

    @Auditado(
            acao = "Cadastro de usuário",
            categoria = CategoriaLog.ADMIN
    )
    public void createUser(@RequestBody @Valid CreateUser data) {
        if (validateUsername(data.getUsername())) throw new UsernameDuplicatedException(data.getUsername());
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
        detalhes.setDetalhes("Cadastro de usuário feito por administrador");
    }

    @Auditado(
            acao = "Login de usuário",
            categoria = CategoriaLog.AUDITORIA)
    public LoginResponse logarUsuario(LoginRequest loginRequest) {
        User user = findByUsername(loginRequest.getUsername());
        if (encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String jwt = jwtService.generateToken(user);

            // Constroi o DTO de resposta com o token e informações do usuário
            var loginResponse = LoginResponse.builder()
                    .token(jwt) // O JWT gerado
                    .userId(user.getId()) // ID do usuário
                    .username(user.getUsername()) // Nome de usuário
                    .role(user.getRole().name()) // Papel do usuário (convertido para string)
                    .build();
            detalhes.setDetalhes("Login realizado pelo usuário " + user.getUsername());
            return loginResponse;
        }
        throw new UsernameNotFoundException("Erro ao logar usuário " + loginRequest.getUsername() + " (Usuário e/ou senha incorretos)");
    }


    @Auditado(
            acao = "Edição de dados do usuário",
            categoria = CategoriaLog.ADMIN
    )
    @Transactional
    public void editUser(@PathVariable Long id, @RequestBody CreateUser data) {
        var user = repository.findById(id).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        ///  SE O USUÁRIO ESTIVER RECEBENDO UM USERNAME QUE NÃO É IGUAL AO ATUAL SALVO NO BANCO DE DADOS
        /// PASSA POR UMA VALIDAÇÃO PARA SABER SE JÁ NÃO PERTENCE A UM OUTRO USUÁRIO
        if (!user.getUsername().equals(data.getUsername()) && validateUsername(data.getUsername())) {
            throw new UsernameDuplicatedException(data.getUsername());
        }
        if (StringUtils.isNotBlank(data.getUsername())) user.setUsername(data.getUsername());
        if (data.getRole() != null) user.setRole(data.getRole());
        if (StringUtils.isNotBlank(data.getNome())) user.setNome(data.getNome());
        if (StringUtils.isNotBlank(data.getPassword())) user.setPassword(encoder.encode(data.getPassword()));
        if (data.getAtivo() != null) user.setAtivo(data.getAtivo());
        if (data.getConsentGeoloc() != null) user.setConsentGeoloc(data.getConsentGeoloc());
        detalhes.setDetalhes("Usuário " + user.getUsername() + " teve seus dados alterados pelo administrador");
    }


    @Auditado(
            acao = "Exclusão de usuário",
            categoria = CategoriaLog.ADMIN
    )
    public void deleteUser(@PathVariable Long id) {
        var user = findById(id);
        repository.delete(user);
        detalhes.setDetalhes("Usuário " + user.getUsername() + " Foi deletado por um administrador");
    }

    public User findByUsername(String username) {
        return repository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para o username " + username));
    }


    private boolean validateUsername(String username) {
        return repository.existsByUsername(username);

    }

    public static UserDetails getUserDetailsBySecurityContext() {
        return (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }


    @Auditado(
            acao = "Busca por todos os usuários",
            categoria = CategoriaLog.ADMIN
    )
    public List<UserResponse> findAll() {
        detalhes.setDetalhes("Administrador executando busca por todos usuários");
        return repository.findAll().stream()
                .map(UserResponse::new) // Converte cada User em UserResponse
                .collect(Collectors.toList());
    }


    @Auditado(
            acao = "Busca paginada por usuários",
            categoria = CategoriaLog.ADMIN
    )
    public Page<UserResponse> findAll(int pageNumber) {
        detalhes.setDetalhes("Administrador executando busca por usuários de forma paginada");
        Pageable page = PageRequest.of(pageNumber, 20);
        return repository.findAll(page)
                .map(UserResponse::new); // Converte cada User da página em UserResponse
    }


    public User findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    public PageUserDTO findByNomeLike(String nome, int pageNumber) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        User user = User.builder()
                .nome(nome)
                .build();
        Example<User> example = Example.of(user, matcher);
        var pageEntity = repository.findAll(example, PageRequest.of(pageNumber, 3));
        var pageDTO = pageEntity.stream().map(p -> {
            return new UserDTO(p.getNome(), "Palhaço", p.getAtivo());
        }).toList();
        return new PageUserDTO(pageDTO, pageEntity.getTotalPages());
    }


}
