package com.marcweb.service;

import com.marcweb.auditoria.Auditado;
import com.marcweb.dto.CreateFolga;
import com.marcweb.dto.FolgaResponse;
import com.marcweb.dto.UpdateFolga;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.Folga;
import com.marcweb.entity.Role;
import com.marcweb.entity.User;
import com.marcweb.repository.FolgaRepository;
import com.marcweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolgaService {

    private final FolgaRepository folgaRepository;
    private final UserRepository userRepository; // Para buscar o User pelo ID

    // --- CRUD Methods ---

    @Auditado(
            acao = "Buscando todas as folgas registradas",
            categoria = CategoriaLog.ADMIN,
            detalhes = "Administrador realizando consulta por todos os registros de folga"
    )
    @Transactional(readOnly = true)
    public List<FolgaResponse> findAll() {
        List<Folga> folgas = folgaRepository.findAll();
        return folgas.stream().map(FolgaResponse::new).collect(Collectors.toList());
    }

    @Auditado(
            acao = "Buscando folga especifica por id",
            categoria = CategoriaLog.AUDITORIA,
            detalhes = "Usuário realizando consulta por folga atravéz do id"
    )
    @Transactional(readOnly = true)
    public FolgaResponse findById(Long id) {

        Folga folga = folgaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folga não encontrada com ID: " + id)); // Usando ResponseStatusException para padronizar a resposta HTTP

        // VALIDANDO SE USUÁRIO TEM PERMISSÃO OU NÃO
        validarUsuario(folga.getUser().getId());
        // CASO TENHA, RETORNA A FOLGA
        return new FolgaResponse(folga);
    }

    @Auditado(
            acao = "Buscando todas as folgas de um usuário pelo id do próprio",
            categoria = CategoriaLog.AUDITORIA,
            detalhes = "Administrador realizando consulta por todos os registros de folga"
    )
    @Transactional(readOnly = true)
    public List<FolgaResponse> findByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado com ID: " + userId)); // Usando ResponseStatusException para padronizar a resposta HTTP
        // VALIDANDO SE USUÁRIO TEM PERMISSÃO OU NÃO
        validarUsuario(userId);

        //List<Folga> allUserFolgas = folgaRepository.findByUser(user);
        List<Folga> folgas = folgaRepository.findByUserId(userId);
        return folgas.stream().map(FolgaResponse::new).collect(Collectors.toList());
    }

    // PARA REQUISIÇÕES ONDE APENAS O TITULAR OU UM ADM PODEM ACESSAR
    private void validarUsuario(Long idTitular) {
        // COLETANDO USUÁRIO LOGADO NO CONTEXTO DE SEGURANÇA
        UserDetails useDetails = UserService.getUserDetailsBySecurityContext();
        // VERIFICANDO SE USUÁRIO É ADMIN
        boolean isAdm = useDetails.getAuthorities().stream().findFirst().toString().contains(Role.ADMIN.name());

        //  VERIFICANDO SE O ID DO TITULAR É IGUAL AO DO USUÁRIO LOGADO
        boolean isTitular = idTitular.equals(userRepository.findIdByUsername(useDetails.getUsername()).orElse(null));
        // SE O USUÁRIO NÃO FOR O TITULAR E NEM ADM -> ERRO
        if (!isTitular && !isAdm) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
    }


    @Auditado(
            acao = "Registro de folga para usuário",
            categoria = CategoriaLog.ADMIN,
            detalhes = ""
    )
    @Transactional
    public String create(CreateFolga dados) {
        User user = userRepository.findById(dados.userId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado com ID: " + dados.userId()));

        // Validação de sobreposição - Usando método que será adicionado ao FolgaRepository
        if (existeSobreposicao(user, dados.dataInicio(), dados.dataFim(), null)) { // null para ID, pois é um novo registro
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma folga registrada para o usuário " + user.getNome() + " no período de " + dados.dataInicio() + " a " + dados.dataFim() + "."); // Usando CONFLICT para sobreposição
        }

        Folga folga = new Folga(user, dados);

        Folga savedFolga = folgaRepository.save(folga);
        return "Folga registrada para o usuário " + user.getUsername();
    }


    @Auditado(
            acao = "Editando dados de uma folga ",
            categoria = CategoriaLog.ADMIN,
            detalhes = "Administrador alterando dados de uma folga"
    )
    @Transactional
    public FolgaResponse update(Long id, UpdateFolga dados) {
        Folga folga = folgaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folga não encontrada com ID: " + id));

        // Validação de sobreposição (exceto para o próprio registro sendo atualizado)
        if (existeSobreposicao(folga.getUser(), dados.dataInicio(), dados.dataFim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A atualização resultaria em sobreposição com outra folga registrada para o usuário " + folga.getUser().getNome() + ".");
        }

        // Atualiza os campos
        folga.updateFolga(dados);

        Folga updatedFolga = folgaRepository.save(folga);
        return new FolgaResponse(updatedFolga);
    }


    @Auditado(
            acao = "Deletando folga",
            categoria = CategoriaLog.ADMIN,
            detalhes = "Administrador deletando folga"
    )
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

    public boolean existsByUserAndDate(Long idUser, LocalDate data) {
        return folgaRepository.existsByUserIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual
                (idUser, data, data);
    }

    // BUSCANDO FOLGAS EM UM DETERMINADO PERIODO
    public List<Folga> findByPeriodo(Long userId, LocalDate start, LocalDate end) {
        return folgaRepository.findByUserIdAndDataInicioGreaterThanEqualAndDataFimLessThanEqual(
                userId, start, end
        );
    }

    // --- Validation Helper ---

    /**
     * Verifica se há sobreposição de datas para um usuário específico.
     *
     * @param user       Usuário a ser verificado.
     * @param dataInicio Nova data de início.
     * @param dataFim    Nova data de fim.
     * @param folgaId    ID da folga sendo atualizada (ou null se for uma nova folga).
     * @return true se houver sobreposição, false caso contrário.
     */
    private boolean existeSobreposicao(User user, LocalDate dataInicio, LocalDate dataFim, Long folgaId) {
        List<Folga> sobrepostas = folgaRepository.findByUserAndPeriodoSobrepostoExcluindoId(user.getId(), dataInicio, dataFim, folgaId); // Passando ID do usuário, não o objeto

        return !sobrepostas.isEmpty();
    }
}