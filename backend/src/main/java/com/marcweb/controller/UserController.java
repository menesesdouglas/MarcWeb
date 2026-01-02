package com.marcweb.controller;

import com.marcweb.dto.CreateUser;
import com.marcweb.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService service;

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createUser(@RequestBody @Valid CreateUser data) {
       service.createUser(data);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
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




}
