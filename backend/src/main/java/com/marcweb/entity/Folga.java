package com.marcweb.entity;

import com.marcweb.dto.CreateFolga;
import com.marcweb.dto.SolicitaFolga;
import com.marcweb.dto.UpdateFolga;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;

@Entity
@Table(name = "folgas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Folga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Relacionamento com User

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio; // Data de início da folga

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim; // Data de término da folga

    @Column(nullable = false, length = 500)
    private String justificativa; // Justificativa para a folga

    // Novo campo: tipo de folga
    @Enumerated(EnumType.STRING) // Armazena o nome do enum como string no banco
    @Column(nullable = false, length = 30) // Define um tamanho razoável
    private TipoFolga tipo; // Tipo da folga (FERIAS, ATESTADO, FALTA_JUSTIFICADA)


    private boolean aprovada;

    /// PARA SER USADO QUANDO UM ADMIN CADASTRAR UMA FOLGA
    public Folga(User user, CreateFolga dados) {
        this.user = user;
        BeanUtils.copyProperties(dados, this);
        this.aprovada = true;
    }

    /// PARA SER USADO QUANDO USUÁRIO SOLICITAR FOLGA
    public Folga(User user, SolicitaFolga dados) {
        this.user = user;
        BeanUtils.copyProperties(dados, this);
        this.aprovada = false;
    }



    public void updateFolga(UpdateFolga novosDados) {
        BeanUtils.copyProperties(novosDados, this);
    }
}