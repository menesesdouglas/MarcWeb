package com.marcweb.entity;

import com.marcweb.dto.feriado.CreateFeriado;
import com.marcweb.dto.feriado.FeriadoNacionalDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;

@Entity
@Table(name = "feriados")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feriado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data; // Data do feriado

    @Column(nullable = false)
    private String nome; // Nome do feriado

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemFeriado origem; // API (BrasilAPI), MANUAL


    public Feriado(CreateFeriado data, OrigemFeriado origem) {
        BeanUtils.copyProperties(data, this);
        this.origem = origem;
    }

    // USADO PARA CADASTRO AUTOMÁTICO DE FERIADOS NACIONAIS
    public Feriado(FeriadoNacionalDTO dadosFeriado) {
        this.nome = dadosFeriado.name();
        this.data = dadosFeriado.date();
        this.origem = OrigemFeriado.API;
    }


    // USADO PARA RECEBER REGISTROS SEM REFERENCIA DE ID
    public Feriado(LocalDate data, String nome, OrigemFeriado origem) {
        this.data = data;
        this.nome = nome;
        this.origem = origem;
    }
}