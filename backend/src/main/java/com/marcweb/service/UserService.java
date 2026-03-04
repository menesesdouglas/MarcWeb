package com.marcweb.service;

import com.marcweb.auditoria.Auditado;
import com.marcweb.auditoria.Detalhes;
import com.marcweb.dto.folga.FolgaPendenteDetalhada;
import com.marcweb.dto.login.LoginRequest;
import com.marcweb.dto.login.LoginResponse;
import com.marcweb.dto.user.*;
import com.marcweb.entity.*;
import com.marcweb.exceptions.LimiteFuncionarioAtivosException;
import com.marcweb.exceptions.UsernameDuplicatedException;
import com.marcweb.exceptions.UsuarioBloqueadoException;
import com.marcweb.repository.FolgaRepository;
import com.marcweb.repository.UserRepository;
import com.marcweb.security.JwtService;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.stream.Collectors;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {


    private final ConfigEmpresaService configEmpresaService;
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final FolgaRepository folgaRepository;
    private final Detalhes detalhes;

    @Auditado(
            acao = "Cadastro de usuário",
            categoria = CategoriaLog.ADMIN
    )
    public void createUser(@RequestBody @Valid CreateUser data) {
        /// VALIDANDO USERNAME
        if (validateUsername(data.getUsername())) throw new UsernameDuplicatedException(data.getUsername());
        Boolean consent = data.getConsentGeoloc();

        // Obter o tenantId do TenantContext
        String tenantId = com.marcweb.hibernate.TenantContext.getCurrentTenant();

        User user = User.builder()
                .ativo(true)
                .nome(data.getNome())
                .username(data.getUsername())
                .cpf(data.getCpf())
                .role(data.getRole())
                .password(encoder.encode(data.getPassword()))
                .consentGeoloc(consent != null ? consent : false)
                .cargo(data.getCargo())
                .tenantId(tenantId) // Associa o usuário ao tenant atual
                .build();
        User savedUser = repository.save(user);
        /// VALIDANDO QUANTIDADE DE USUÁRIOS ATIVOS DE ACORDO COM O PLANO
        try {
            if (data.getRole().equals(Role.EMPLOYEE)) validarPlano();
        } catch (LimiteFuncionarioAtivosException e) {
            /// DELETANDO USUÁRIO CASO TENHA ULTRAPASSADO O LIMITE
            repository.delete(savedUser);
            throw new LimiteFuncionarioAtivosException(e.getMessage());
        }

        detalhes.setDetalhes("Cadastro de usuário feito por administrador");
    }

    @Auditado(
            acao = "Login de usuário",
            categoria = CategoriaLog.AUDITORIA)
    public LoginResponse logarUsuario(LoginRequest loginRequest, String tenantId) {
        // O tenantId é fornecido via header X-Tenant-Id
        // A busca por usuário precisa ser feita considerando o tenant atual
        User user = findByUsernameAndTenant(loginRequest.getUsername(), tenantId);
        if (!user.getAtivo()) throw new RuntimeException("Erro ao logar com este usuário");
        if (encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String jwt = jwtService.generateToken(user, tenantId);

            // Constroi o DTO de resposta com o token e informações do usuário
            var loginResponse = LoginResponse.builder()
                    .token(jwt) // O JWT gerado
                    .userId(user.getId()) // ID do usuário
                    .username(user.getUsername()) // Nome de usuário
                    .role(user.getRole().name()) // Papel do usuário (convertido para string)
                    .build();
            detalhes.setDetalhes("Login realizado pelo usuário " + user.getUsername());
            return loginResponse;
        }
        throw new UsernameNotFoundException("Erro ao logar usuário " + loginRequest.getUsername() + " (Usuário e/ou senha incorretos)");
    }

    /**
     * Busca usuário considerando o tenant atual (para login)
     */
    public User findByUsernameAndTenant(String username, String tenantId) {
        // O JPA usa o contexto de tenant configurado no TenantContext
        var user = repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para o username " + username));
        if (!user.getAtivo()) throw new UsernameNotFoundException("O usuário " + username + " foi desligado");

        // Verifica se o usuário pertence ao tenant correto
        if (tenantId != null && !tenantId.equals(user.getTenantId())) {
            throw new UsernameNotFoundException("Usuário não encontrado para o tenant " + tenantId);
        }

        return user;
    }


    @Auditado(
            acao = "Alteração de dados do usuário",
            categoria = CategoriaLog.ADMIN
    )
    public String editUser(Long id, UpdateUserDTO data) {
        String message = "";
        boolean deveValidar = false;
        var user = repository.findById(id).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }

        ///  SE O USUÁRIO ESTIVER RECEBENDO UM USERNAME QUE NÃO É IGUAL AO ATUAL SALVO NO BANCO DE DADOS
        /// PASSA POR UMA VALIDAÇÃO PARA SABER SE JÁ NÃO PERTENCE A UM OUTRO USUÁRIO
        if (!user.getUsername().equals(data.getUsername()) && validateUsername(data.getUsername())) {
            throw new UsernameDuplicatedException(data.getUsername());
        }
        if (StringUtils.isNotBlank(data.getUsername())) user.setUsername(data.getUsername());
        if (StringUtils.isNotBlank(data.getNome())) user.setNome(data.getNome());
        if (StringUtils.isNotBlank(data.getPassword())) user.setPassword(encoder.encode(data.getPassword()));
        if (data.getAtivo() != null && !data.getAtivo().equals(user.getAtivo())) {
            deveValidar = data.getAtivo();
            /// ALTERANDO SITUAÇÃO DO CADASTRO
            user.setAtivo(data.getAtivo());
            if (data.getAtivo()) {
                if (user.getAtivacoes() > 3) throw new UsuarioBloqueadoException();
                user.setAtivacoes(user.getAtivacoes() + 1);
            }
            message = user.getAtivacoes() < 4 ? "Atenção, este usuário só poderá ser reativado mais " + (4 - user.getAtivacoes()) + " vezes"
                    : "Atenção, este usuário não poderá ser ativado novamente";
        }
        if (data.getConsentGeoloc() != null) user.setConsentGeoloc(data.getConsentGeoloc());
        if (StringUtils.isNotBlank(data.getCargo())) user.setCargo(data.getCargo());
        if (StringUtils.isNotBlank(data.getCpf())) user.setCpf(data.getCpf());
        repository.save(user);
        /// VALIDANDO QUANTIDADE DE USUÁRIOS ATIVOS DE ACORDO COM O PLANO
        if (deveValidar) {
            try {
                validarPlano();
            } catch (LimiteFuncionarioAtivosException e) {
                user.setAtivo(false);
                repository.save(user);
                throw new LimiteFuncionarioAtivosException(e.getMessage());
            }
        }
        detalhes.setDetalhes("Usuário " + user.getUsername() + " teve seus dados alterados pelo administrador");
        return message;
    }


    @Auditado(
            acao = "Exclusão de usuário",
            categoria = CategoriaLog.ADMIN
    )
    @Transactional
    public void deleteUser(@PathVariable Long id) {
        var user = findById(id);
        user.setAtivo(false);
        detalhes.setDetalhes("Usuário " + user.getUsername() + " Foi deletado por um administrador");
    }

    ///  SÓ É,LIBERADO SE O USUÁRIO ESTIVER ATIVO
    public User findByUsername(String username) {
        var user = repository.findByUsernameIgnoreCase(username).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para o username " + username));
        if (!user.getAtivo()) throw new RuntimeException("Erro ao buuscar usuário");
        return user;
    }


    private boolean validateUsername(String username) {
        return repository.existsByUsername(username);

    }

    public static UserDetails getUserDetailsBySecurityContext() {
        return (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }


    @Auditado(
            acao = "Busca por todos os usuários",
            categoria = CategoriaLog.ADMIN
    )
    public List<UserResponse> findAll() {
        detalhes.setDetalhes("Administrador executando busca por todos usuários");
        return repository.findAll().stream()
                .map(UserResponse::new) // Converte cada User em UserResponse
                .collect(Collectors.toList());
    }


    @Auditado(
            acao = "Busca paginada por usuários",
            categoria = CategoriaLog.ADMIN
    )
    public Page<UserResponse> findAll(int pageNumber) {
        detalhes.setDetalhes("Administrador executando busca por usuários de forma paginada");
        Pageable page = PageRequest.of(pageNumber, 20);
        return repository.findAll(page)
                .map(UserResponse::new); // Converte cada User da página em UserResponse
    }


    public User findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }


    // NOVO Método: Obter dados do usuário autenticado
    public UserResponse getMe() {
        UserDetails userDetails = getUserDetailsBySecurityContext();
        User user = findByUsername(userDetails.getUsername());
        return new UserResponse(user);
    }

    // NOVO Método: Atualizar perfil do usuário autenticado
    @Auditado(
            acao = "Atualização de perfil do usuário",
            categoria = CategoriaLog.AUDITORIA
    )
    @Transactional
    public void updateProfile(UpdateProfileRequest data) {
        UserDetails userDetails = getUserDetailsBySecurityContext();
        User user = findByUsername(userDetails.getUsername());

        // Atualizar o nome
        if (StringUtils.isNotBlank(data.getNome())) {
            user.setNome(data.getNome());
        }

        // Atualizar o cargo (apenas administradores podem alterar o cargo de outros, 
        // mas usuários podem atualizar o próprio cargo se enviado)
        if (StringUtils.isNotBlank(data.getCargo())) {
            user.setCargo(data.getCargo());
        }

        // Apenas administradores podem alterar o role
        if (data.getRole() != null && !user.getRole().equals(data.getRole())) {
            throw new RuntimeException("Apenas administradores podem alterar o cargo");
        }

        // Atualizar senha: verificar senha atual antes de definir a nova
        if (StringUtils.isNotBlank(data.getNewPassword())) {
            if (StringUtils.isBlank(data.getCurrentPassword())) {
                throw new RuntimeException("Senha atual é obrigatória para alterar a senha");
            }
            if (!encoder.matches(data.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Senha atual incorreta");
            }
            user.setPassword(encoder.encode(data.getNewPassword()));
        }

        detalhes.setDetalhes("Usuário " + user.getUsername() + " atualizou seu perfil");
    }

    // NOVO Método: Atualizar CPF do usuário
    @Auditado(
            acao = "Atualização de CPF do usuário",
            categoria = CategoriaLog.ADMIN
    )
    @Transactional
    public void updateCpf(Long userId, String cpf) {
        User user = findById(userId);
        user.setCpf(cpf);
        detalhes.setDetalhes("CPF do usuário " + user.getUsername() + " foi atualizado pelo administrador");
    }


    @Transactional
    public ProfileUser loadUserProfile(Long id, int pageNumber) {
        /// TROCAR A EXCEPTION DEPOIS
        ProfileUser profile = repository.findProfileById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Page<Folga> folgasPaginadas = folgaRepository.findByUserId(id, PageRequest.of(pageNumber, 20));
        /// USANDO CLASSE DE FOLGA PENDENTE PARA APROVEITAR O MODELO
        List<FolgaPendenteDetalhada> folgasDto = folgasPaginadas.stream().map(FolgaPendenteDetalhada::new).toList();
        profile.setFolgas(folgasDto);
        profile.setTotalPagesFolgas(folgasPaginadas.getTotalPages());
        return profile;
    }

    ///  VALIDANDO QUANTIDADE DE USUÁRIOS DE ACORDO COM O PLANO DA EMPRESA
    private void validarPlano() {
        Plano plano = configEmpresaService.findPlanoEmpresa();
        if (!plano.equals(Plano.ENTERPRISE)) {
            long qtdFuncionariosAtivos = repository.countAtivos();
            if (plano.equals(Plano.FREEMIUM) && qtdFuncionariosAtivos > 3)
                throw new LimiteFuncionarioAtivosException(
                        "Seu plano FREEMIUM permite até 3 funcionários ativos. Faça upgrade para o plano BÁSICO (até 15 funcionários)."
                );
            if (plano.equals(Plano.BASICO) && qtdFuncionariosAtivos > 15)
                throw new LimiteFuncionarioAtivosException(
                        "Seu plano BÁSICO permite até 15 funcionários ativos. Faça upgrade para o plano PREMIUM (até 29 funcionários)."
                );
            if (plano.equals(Plano.PREMIUM) && qtdFuncionariosAtivos > 29)
                throw new LimiteFuncionarioAtivosException(
                        "Seu plano PREMIUM permite até 29 funcionários ativos. Entre em contato para contratar o plano ilimitado."
                );
        }
    }
}
