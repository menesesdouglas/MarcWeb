package com.marcweb.dto;

import com.marcweb.entity.ConfigEmpresa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor
@Data
public class DataConfigEmpresa {

    private Long id; // Adicionando ID para mapeamento completo, se necessário

    @NotBlank(message = "Campo obrigatório")
    private String nomeEmpresa;

    @NotNull(message = "Campo obrigatório")
    private Double latCentro;

    @NotNull(message = "Campo obrigatório")
    private Double longCentro;

    @NotNull(message = "Campo obrigatório")
    private Integer raioGeoloc;

    private Boolean geolocAtiva;

    public DataConfigEmpresa(ConfigEmpresa configEmpresa) {
        BeanUtils.copyProperties(configEmpresa, this);
    }

    // Método para converter DTO em Entidade
    public ConfigEmpresa toEntity() {
        ConfigEmpresa entity = new ConfigEmpresa();
        // Copia as propriedades do DTO para a entidade
        entity.setId(this.getId());
        entity.setNomeEmpresa(this.getNomeEmpresa());
        entity.setLatCentro(this.getLatCentro());
        entity.setLongCentro(this.getLongCentro());
        entity.setRaioGeoloc(this.getRaioGeoloc());
        entity.setGeolocAtiva(this.getGeolocAtiva());
        return entity;
    }
}