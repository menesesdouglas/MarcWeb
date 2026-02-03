package com.marcweb.service;

import com.marcweb.auditoria.Auditado;
import com.marcweb.auditoria.Detalhes;
import com.marcweb.dto.folga.*;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolgaService {

    private final FolgaRepository folgaRepository;
    private final UserRepository userRepository;
    private final Detalhes detalhes;

    @Auditado(
            acao = "Buscando folga especifica por id",
            categoria = CategoriaLog.AUDITORIA
    )
    @Transactional(readOnly = true)
    public FolgaResponse findById(Long id) {
        detalhes.setDetalhes("Usuário realizou consulta de folga pelo id " + id);
        Folga folga = folgaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folga não encontrada com ID: " + id)); // Usando ResponseStatusException para padronizar a resposta HTTP

        // VALIDANDO SE USUÁRIO TEM PERMISSÃO OU NÃO
        validarUsuario(folga.getUser().getId());
        // CASO TENHA, RETORNA A FOLGA
        return new FolgaResponse(folga);
    }

    @Auditado(
            acao = "Buscando todas as folgas de um usuário pelo id do próprio",
            categoria = CategoriaLog.AUDITORIA)
    @Transactional(readOnly = true)
    public List<FolgaResponse> findByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado com ID: " + userId)); // Usando ResponseStatusException para padronizar a resposta HTTP
        // VALIDANDO SE USUÁRIO TEM PERMISSÃO OU NÃO
        validarUsuario(userId);
        detalhes.setDetalhes("Consulta por todas as folgas do usuário " + user.getUsername());
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
            categoria = CategoriaLog.ADMIN)
    @Transactional
    public String create(CreateFolga dados) {
        User user = userRepository.findById(dados.userId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado com ID: " + dados.userId()));

        // Validação de sobreposição - Usando método que será adicionado ao FolgaRepository
        if (existeSobreposicao(user, dados.dataInicio(), dados.dataFim(), null)) { // null para ID, pois é um novo registro
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma folga registrada para o usuário " + user.getNome() + " no período de " + dados.dataInicio() + " a " + dados.dataFim() + "."); // Usando CONFLICT para sobreposição
        }

        Folga folga = new Folga(user, dados);

        Folga savedFolga = folgaRepository.save(folga);
        detalhes.setDetalhes("Administrador registrou folga para o usuário " + user.getUsername() + " de " + dados.dataInicio().toString() + " à " + dados.dataFim().toString());
        return "Folga registrada para o usuário " + user.getUsername();
    }


    public void solicitarFolga(SolicitaFolga dados, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado para o username: " + username));

        // Validação de sobreposição - Usando método que será adicionado ao FolgaRepository
        if (existeSobreposicao(user, dados.dataInicio(), dados.dataFim(), null)) { // null para ID, pois é um novo registro
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma folga registrada para o usuário " + user.getNome() + " no período de " + dados.dataInicio() + " a " + dados.dataFim() + "."); // Usando CONFLICT para sobreposição
        }

        Folga folgaSolicitada = new Folga(user, dados);

        folgaRepository.save(folgaSolicitada);
    }


    @Auditado(
            acao = "Editando dados de uma folga ",
            categoria = CategoriaLog.ADMIN
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
        detalhes.setDetalhes("Administrador alterou dados da folga do usuário " + folga.getUser().getUsername() + ", que estava marcada para " + folga.getDataInicio().toString() + ". \n Novos dados: " + dados);
        return new FolgaResponse(updatedFolga);
    }


    @Auditado(
            acao = "Deletando folga",
            categoria = CategoriaLog.ADMIN
    )
    @Transactional
    public void deleteById(Long id) {
        var folga = folgaRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folga não encontrada com ID: " + id)
        );

        try {
            folgaRepository.deleteById(id);
            detalhes.setDetalhes("Administrador deletou a folga do usuário " + folga.getUser().getUsername() + " marcada entre os dias " + folga.getDataInicio().toString() + " e " + folga.getDataFim().toString());
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Folga não encontrada com ID: " + id);
        }
    }

    public boolean existsByUserAndDate(Long idUser, LocalDate data) {
        return folgaRepository.existsByUserIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqual
                (idUser, data, data);
    }

    /// BUSCANDO FOLGAS APROVADAS EM UM DETERMINADO PERIODO
    public List<Folga> findByPeriodo(Long userId, LocalDate start, LocalDate end) {
        return folgaRepository.findByUserIdAndDataInicioGreaterThanEqualAndDataFimLessThanEqualAndAprovada(
                userId, start, end, true
        );
    }

    // NOVO Método: Listar folgas do usuário autenticado
    public List<FolgaResponse> findByMe() {
        UserDetails userDetails = UserService.getUserDetailsBySecurityContext();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new RuntimeException("Usuário não encontrado")
        );
        List<Folga> folgas = folgaRepository.findByUserId(user.getId());
        return folgas.stream().map(FolgaResponse::new).collect(Collectors.toList());
    }

    // NOVO Método: Listar folgas pendentes com detalhes completos
    @Auditado(
            acao = "Buscando todas as folgas pendentes com detalhes",
            categoria = CategoriaLog.ADMIN
    )
    @Transactional(readOnly = true)
    public List<FolgaPendenteDetalhada> findFolgasPendentesDetalhadas() {
        detalhes.setDetalhes("Administrador realizou consulta por todas as folgas pendentes com detalhes");
        List<Folga> folgas = folgaRepository.findFolgasPendentes();
        return folgas.stream().map(FolgaPendenteDetalhada::new).collect(Collectors.toList());
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


    @Auditado(
            acao = "Aprovando folga",
            categoria = CategoriaLog.ADMIN
    )
    @Transactional
    public boolean solucionarFolga(Long id, boolean aprovada) { /// APROVANDO OU NÃO UYA FOLGA
        var folgaSolicitada = folgaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folga não enontrada"));
        if(folgaSolicitada.getAprovada() != null) return false;
        folgaSolicitada.setAprovada(aprovada);
        detalhes.setDetalhes("Aprovando folga no dia " + folgaSolicitada.getDataInicio().format(DateTimeFormatter.ofPattern("dd/MM/yy")) + " para " + folgaSolicitada.getUser().getNome());
        return true;
    }
}