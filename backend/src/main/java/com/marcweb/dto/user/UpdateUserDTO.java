package com.marcweb.dto.user;

import lombok.Data;

@Data
public class UpdateUserDTO {
    private String username;
    private String nome;
    private String password;
    private Boolean ativo;
    private Boolean consentGeoloc;
    private String cargo;
    private String cpf;
}
