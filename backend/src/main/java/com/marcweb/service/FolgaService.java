package com.marcweb.service;

import com.marcweb.dto.CreateFolga;
import com.marcweb.dto.FolgaResponse;
import com.marcweb.dto.UpdateFolga;
import com.marcweb.entity.Folga;
import com.marcweb.entity.TipoFolga;
import com.marcweb.entity.User;
import com.marcweb.repository.FolgaRepository;
import com.marcweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException; 
import org.springframework.http.HttpStatus; 
import org.springframework.dao.EmptyResultDataAccessException; 

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException; 
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolgaService {

    private final FolgaRepository folgaRepository;
    private final UserRepository userRepository; // Para buscar o User pelo ID

    // --- CRUD Methods ---

    @Transactional(readOnly = true)
    public List<FolgaResponse> findAll() {
        List<Folga> folgas = folgaRepository.findAll();
        return folgas.stream()
                .map(FolgaResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FolgaResponse findById(Long id) {
        Folga folga = folgaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folga não encontrada com ID: " + id)); // Usando ResponseStatusException para padronizar a resposta HTTP
        return new FolgaResponse(folga);
    }

    @Transactional(readOnly = true)
    public List<FolgaResponse> findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado com ID: " + userId)); // Usando ResponseStatusException para padronizar a resposta HTTP

        List<Folga> allUserFolgas = folgaRepository.findByUser(user);
        List<Folga> folgas = folgaRepository.findByUserId(userId);
        return folgas.stream()
                .map(FolgaResponse::new)
                .collect(Collectors.toList());
    }


    @Transactional
    public FolgaResponse create(CreateFolga dados) {
        User user = userRepository.findById(dados.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado com ID: " + dados.userId()));

        // Validação de sobreposição - Usando método que será adicionado ao FolgaRepository
        if (existeSobreposicao(user, dados.dataInicio(), dados.dataFim(), null)) { // null para ID, pois é um novo registro
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma folga registrada para o usuário " + user.getNome() + " no período de " + dados.dataInicio() + " a " + dados.dataFim() + "."); // Usando CONFLICT para sobreposição
        }

        Folga folga = Folga.builder()
                .user(user)
                .dataInicio(dados.dataInicio())
                .dataFim(dados.dataFim())
                .tipo(dados.tipo())
                .justificativa(dados.justificativa())
                .build();

        Folga savedFolga = folgaRepository.save(folga);
        return new FolgaResponse(savedFolga);
    }

    @Transactional
    public FolgaResponse update(Long id, UpdateFolga dados) {
        Folga folga = folgaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folga não encontrada com ID: " + id));

        // Validação de sobreposição (exceto para o próprio registro sendo atualizado)
        if (existeSobreposicao(folga.getUser(), dados.dataInicio(), dados.dataFim(), id)) {
             throw new ResponseStatusException(HttpStatus.CONFLICT, "A atualização resultaria em sobreposição com outra folga registrada para o usuário " + folga.getUser().getNome() + ".");
        }

        // Atualiza os campos
        folga.setDataInicio(dados.dataInicio());
        folga.setDataFim(dados.dataFim());
        folga.setTipo(dados.tipo());
        folga.setJustificativa(dados.justificativa());

        Folga updatedFolga = folgaRepository.save(folga);
        return new FolgaResponse(updatedFolga);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!folgaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Folga não encontrada com ID: " + id); 
        }
        try {
            folgaRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Folga não encontrada com ID: " + id);
        }
    }

    // --- Validation Helper ---

    /**
     * Verifica se há sobreposição de datas para um usuário específico.
     * @param user Usuário a ser verificado.
     * @param dataInicio Nova data de início.
     * @param dataFim Nova data de fim.
     * @param folgaId ID da folga sendo atualizada (ou null se for uma nova folga).
     * @return true se houver sobreposição, false caso contrário.
     */
    private boolean existeSobreposicao(User user, LocalDate dataInicio, LocalDate dataFim, Long folgaId) {
        List<Folga> sobrepostas = folgaRepository.findByUserAndPeriodoSobrepostoExcluindoId(
                user.getId(), dataInicio, dataFim, folgaId); // Passando ID do usuário, não o objeto

        return !sobrepostas.isEmpty();
    }
}