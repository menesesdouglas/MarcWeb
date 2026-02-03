package com.marcweb.controller;

import com.marcweb.dto.folga.*;
import com.marcweb.service.FolgaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folgas") // Define o caminho base para os endpoints de folga
@RequiredArgsConstructor
public class FolgaController {

    private final FolgaService folgaService;

    // --- Endpoints Públicos (requerem autenticação) ---

    // GET /api/folgas - Listar todas as folgas pendentes (requer ROLE_ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pendentes")
    public ResponseEntity<List<FolgaPendenteDetalhada>> getFolgasPendentes() {
        List<FolgaPendenteDetalhada> folgasPendentes = folgaService.findFolgasPendentesDetalhadas();
        return ResponseEntity.ok(folgasPendentes);
    }

    // GET /api/folgas/{id} - Obter folga específica por ID
    @GetMapping("/{id}")
    public ResponseEntity<FolgaResponse> getFolgaById(@PathVariable Long id) {
        FolgaResponse folga = folgaService.findById(id);
        return ResponseEntity.ok(folga);
    }

    // GET /api/folgas/user/{userId} - Listar folgas de um usuário específico
    // Novamente, a verificação de se o USER pode ver *as próprias* deve ser feita no service ou via Spring Security
    // Exemplo: if (user logado != userId && !hasRole('ADMIN')) then throw 403
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FolgaResponse>> getFolgasByUserId(@PathVariable Long userId) {
        List<FolgaResponse> folgas = folgaService.findByUserId(userId);
        return ResponseEntity.ok(folgas);
    }

    // NOVO Endpoint: Listar folgas do usuário autenticado
    @GetMapping("/me")
    public ResponseEntity<List<FolgaResponse>> getFolgasByMe() {
        List<FolgaResponse> folgas = folgaService.findByMe();
        return ResponseEntity.ok(folgas);
    }

    // --- Endpoints de Modificação (requerem ROLE_ADMIN) ---

    // POST /api/folgas - Criar uma nova folga
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Retorna 201 Created
    public ResponseEntity<String> createFolga(@RequestBody @Valid CreateFolga dados) {
        String resultado = folgaService.create(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
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

    @PostMapping("/solicitar")
    public void solicitarFolga(@RequestBody SolicitaFolga dados, Authentication authentication) {
        folgaService.solicitarFolga(dados, authentication.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/solucionar/{id}/{aprovada}")
    public ResponseEntity<String> aprovarFolga(@PathVariable Long id, @PathVariable boolean aprovada) {
        return folgaService.solucionarFolga(id, aprovada) ?
                ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.CONFLICT).body("Essa folga já teve uma resolução");
    }

}
