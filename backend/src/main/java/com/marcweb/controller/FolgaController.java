package com.marcweb.controller;

import com.marcweb.dto.CreateFolga;
import com.marcweb.dto.FolgaResponse;
import com.marcweb.dto.UpdateFolga;
import com.marcweb.service.FolgaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/folgas") // Define o caminho base para os endpoints de folga
@RequiredArgsConstructor
public class FolgaController {

    private final FolgaService folgaService;

    // --- Endpoints Públicos (requerem autenticação) ---

    // GET /api/folgas - Listar todas as folgas (requer ROLE_ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<FolgaResponse>> getAllFolgas() {
        List<FolgaResponse> folgas = folgaService.findAll();
        return ResponseEntity.ok(folgas);
    }

    // GET /api/folgas/{id} - Obter folga específica por ID
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Admin e o próprio usuário podem ver
    // A lógica de verificação de permissão (se o usuário é o dono) pode ser implementada no service
    // ou via @PostAuthorize, mas para simplicidade inicial, deixamos ADMIN com acesso total.
    // Para acesso mais refinado, o service pode verificar o usuário logado.
    @GetMapping("/{id}")
    public ResponseEntity<FolgaResponse> getFolgaById(@PathVariable Long id) {
        FolgaResponse folga = folgaService.findById(id);
        return ResponseEntity.ok(folga);
    }

    // GET /api/folgas/user/{userId} - Listar folgas de um usuário específico
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Admin pode ver qualquer um, USER pode ver as próprias
    // Novamente, a verificação de se o USER pode ver *as próprias* deve ser feita no service ou via Spring Security
    // Exemplo: if (user logado != userId && !hasRole('ADMIN')) then throw 403
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FolgaResponse>> getFolgasByUserId(@PathVariable Long userId) {
        List<FolgaResponse> folgas = folgaService.findByUserId(userId);
        return ResponseEntity.ok(folgas);
    }

    // --- Endpoints de Modificação (requerem ROLE_ADMIN) ---

    // POST /api/folgas - Criar uma nova folga
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Retorna 201 Created
    public ResponseEntity<FolgaResponse> createFolga(@RequestBody @Valid CreateFolga dados) {
        FolgaResponse novaFolga = folgaService.create(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaFolga);
    }

    // PUT /api/folgas/{id} - Atualizar uma folga existente
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FolgaResponse> updateFolga(@PathVariable Long id, @RequestBody @Valid UpdateFolga dados) {
        FolgaResponse folgaAtualizada = folgaService.update(id, dados);
        return ResponseEntity.ok(folgaAtualizada);
    }

    // DELETE /api/folgas/{id} - Deletar uma folga
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Retorna 204 No Content
    public ResponseEntity<Void> deleteFolga(@PathVariable Long id) {
        folgaService.deleteById(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}