package com.marcweb.dto.config_empresa;

import com.marcweb.entity.ConfigEmpresa;
import com.marcweb.entity.Plano;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor
@Data
public class DataConfigEmpresa {


    @NotBlank(message = "Campo obrigatório")
    private String nomeEmpresa;

    @NotNull(message = "Campo obrigatório")
    private Double latCentro;

    @NotNull(message = "Campo obrigatório")
    private Double longCentro;

    @NotNull(message = "Campo obrigatório")
    private Integer raioGeoloc;

    private Boolean geolocAtiva;

    private Plano plano; /// FREEMIUM || BASICO || PREMIUM || ENTREPRISE

    public DataConfigEmpresa(ConfigEmpresa configEmpresa) {
        BeanUtils.copyProperties(configEmpresa, this);
    }

    // Método para converter DTO em Entidade - Simplificado
    public ConfigEmpresa toEntity() {
        ConfigEmpresa entity = new ConfigEmpresa(); // Cria uma nova instância da entidade
        BeanUtils.copyProperties(this, entity);     // Copia as propriedades do DTO para a entidade
        return entity;                              // Retorna a entidade populada
    }
}