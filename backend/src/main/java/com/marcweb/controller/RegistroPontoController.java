package com.marcweb.controller;

<<<<<<< HEAD
import com.marcweb.dto.*;
=======
import com.marcweb.dto.CreateRegistroPonto;
import com.marcweb.dto.PontosRecentes;
import com.marcweb.dto.RegistroPontoResponse;
import com.marcweb.dto.RegistroPontoResponseWithWarning;
import com.marcweb.dto.StatusAtualFuncionario;
import com.marcweb.dto.TimeAdjustmentRequest;
>>>>>>> b4661d13d91741a3856cf44ac5b943c614eca264
import com.marcweb.service.LogAuditoriaService;
import com.marcweb.service.RegistroPontoService;
import com.marcweb.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ponto")
public class RegistroPontoController {

    private final RegistroPontoService service;
    private final LogAuditoriaService logAuditoriaService;
    private final UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<RegistroPontoResponseWithWarning> registrarPonto(@RequestBody @Valid CreateRegistroPonto data, HttpServletRequest request) {
        // Verificar se o usuário tem registros incompletos de dias anteriores
        UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
        Long userId = userService.findByUsername(userDetails.getUsername()).getId();
        boolean hasIncompletePreviousDay = service.temRegistroIncompletoAnterior(userId);

        // Registrar o ponto
        service.registrarPonto(data, request);

        // Retornar resposta com aviso se necessário
        RegistroPontoResponseWithWarning response = new RegistroPontoResponseWithWarning(
                "Ponto batido com sucesso!!!",
                hasIncompletePreviousDay
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<RegistroPontoMe> meusPontos(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        var pontos = service.buscarPontosRegistrados(start, end);
        return !pontos.getRegistros().isEmpty() ? ResponseEntity.ok(pontos) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recentes")
    public ResponseEntity<List<PontosRecentes>> getStatusRecentes() {
        var lista = service.getStatusRecentes();
        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status-atual")
    public ResponseEntity<List<StatusAtualFuncionario>> getStatusAtualFuncionarios() {
        var lista = service.getStatusAtualFuncionarios();
        return ResponseEntity.ok(lista);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ajuste")
    public ResponseEntity<String> ajustarPonto(@RequestBody TimeAdjustmentRequest request) {
        service.ajustarPonto(request);
        return ResponseEntity.ok("Ajuste de ponto realizado com sucesso");
    }
}
