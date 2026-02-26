package com.marcweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configs_empresa")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_empresa", nullable = false, length = 200)
    private String nomeEmpresa;

    @Column(name = "lat_centro", precision = 10) // Latitude do local da empresa
    private Double latCentro;

    @Column(name = "long_centro", precision = 11) // Longitude do local da empresa
    private Double longCentro;

    @Column(name = "raio_geoloc", columnDefinition = "integer default 100") // Raio em metros
    private Integer raioGeoloc;

    @Column(name = "geoloc_ativa", columnDefinition = "boolean default false")
    private Boolean geolocAtiva; // Se a geolocalização é obrigatória ou não
}
