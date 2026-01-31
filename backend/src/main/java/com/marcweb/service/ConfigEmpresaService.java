package com.marcweb.service;

import com.marcweb.auditoria.Auditado;
import com.marcweb.auditoria.Detalhes;
import com.marcweb.dto.config_empresa.DataConfigEmpresa;
import com.marcweb.entity.CategoriaLog;
import com.marcweb.entity.ConfigEmpresa;
import com.marcweb.exceptions.ConfigEmpresaNotFoundException;
import com.marcweb.repository.ConfigEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ConfigEmpresaService {

    private final ConfigEmpresaRepository repository;
    private final Detalhes detalhes;

    @Auditado(
            acao = "Editando configurações da empresa ",
            categoria = CategoriaLog.ADMIN)
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
        detalhes.setDetalhes("Admnistrador realizando edição de dados da empresa");
    }


    @Auditado(
            acao = "Visualizando configurações da empresa ",
            categoria = CategoriaLog.ADMIN)
    public DataConfigEmpresa findConfigsEmpresa() {
        detalhes.setDetalhes("Usuário realizando visualização de dados da empresa");
        var configEmpresa = repository.findFirst().orElseThrow(ConfigEmpresaNotFoundException::new);
        return new DataConfigEmpresa(configEmpresa);
    }
}
