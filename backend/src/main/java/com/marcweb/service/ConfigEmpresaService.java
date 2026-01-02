package com.marcweb.service;

import com.marcweb.dto.DataConfigEmpresa;
import com.marcweb.entity.ConfigEmpresa;
import com.marcweb.exceptions.ConfigEmpresaNotFoundException;
import com.marcweb.repository.ConfigEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ConfigEmpresaService {

    private final ConfigEmpresaRepository repository;

    public void updateConfigEmpresa(DataConfigEmpresa data) {
        Long idConfigEmpresa = repository.findFirst().orElseThrow(ConfigEmpresaNotFoundException::new)
                .getId();
        ConfigEmpresa configEmpresa = ConfigEmpresa.builder()
                .id(idConfigEmpresa)
                .nomeEmpresa(data.getNomeEmpresa())
                .geolocAtiva(data.getGeolocAtiva())
                .latCentro(data.getLatCentro())
                .longCentro(data.getLongCentro())
                .raioGeoloc(data.getRaioGeoloc())
                .build();
        repository.save(configEmpresa);
    }


    public DataConfigEmpresa findConfigsEmpresa() {
        var configEmpresa = repository.findFirst().orElseThrow(ConfigEmpresaNotFoundException::new);
        return new DataConfigEmpresa(configEmpresa);
    }
}
