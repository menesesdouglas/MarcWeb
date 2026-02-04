package com.marcweb.controller;

import com.marcweb.dto.user.CreateUser;
import com.marcweb.dto.user.ProfileUser;
import com.marcweb.dto.user.UserResponse; // Import necessário
import com.marcweb.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // Import para paginação (opcional)
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Import necessário para findAll sem paginação

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService service;


    // NOVO Endpoint: Listar todos os usuários
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> findById() { // Retorna List<UserResponse> sem paginação
        return service.findAll();
    }

    ///  CARREGANDO PERFIL DO USUÁRIO PARA GERENCIAMENTO DO ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ProfileUser loadUserProfile(@PathVariable Long id, @RequestParam int pageNumber) {
        return service.loadUserProfile(id, pageNumber);
    }

    // NOVO Endpoint: Obter dados do usuário autenticado
    @GetMapping("/me")
    public UserResponse getMe() {
        return service.getMe();
    }

    // Endpoint com paginação
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(params = "pageNumber")
    public Page<UserResponse> findAllUsers(@RequestParam int pageNumber) { // Retorna Page<UserResponse> com paginação
        return service.findAll(pageNumber);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createUser(@RequestBody @Valid CreateUser data) {
        service.createUser(data);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    public void editUser(@PathVariable Long id, @RequestBody CreateUser data) {
        service.editUser(id, data);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
    }

    // NOVO Endpoint: Atualizar perfil do usuário autenticado
    @PutMapping("/profile")
    public void updateProfile(@RequestBody @Valid CreateUser data) {
        service.updateProfile(data);
    }

    // NOVO Endpoint: Atualizar CPF do usuário
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/cpf")
    public void updateCpf(@PathVariable Long id, @RequestBody String cpf) {
        service.updateCpf(id, cpf);
    }


}