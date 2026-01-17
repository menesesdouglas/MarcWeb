package com.marcweb.controller;

import com.marcweb.dto.CreateUser;
import com.marcweb.dto.UserResponse; // Import necessário
import com.marcweb.entity.User;
import com.marcweb.repository.UserRepository;
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
    private final UserRepository repository;

    // NOVO Endpoint: Listar todos os usuários
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> findAllUsers() { // Retorna List<UserResponse> sem paginação
        return service.findAll();
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


    // VISUALIZANDO O ENTIDADE DE USUÁRIO
    @GetMapping("/teste")
    public User get() {
        return repository.findById(2L).orElse(null);
    }

}