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
}
