package com.marcweb.controller;

import com.marcweb.dto.CreateUser;
import com.marcweb.dto.PageUserDTO;
import com.marcweb.dto.UserDTO;
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


    // NOVO Endpoint: Listar todos os usuários
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> findAllUsers() { // Retorna List<UserResponse> sem paginação
        return service.findAll();
    }

    // Endpoint alternativo para compatibilidade com frontend
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<UserResponse> findAllUsersAlt() { // Retorna List<UserResponse> sem paginação
        return service.findAll();
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


    /// BUSCA PAGINADA FILTRADA POR NOME
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(params = {"nome", "pageNumber"})
    public PageUserDTO findByNomeLike(@RequestParam(required = false) String nome, @RequestParam int pageNumber) {
        return service.findByNomeLike(nome, pageNumber);
    }

}
